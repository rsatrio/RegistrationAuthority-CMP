#!/bin/bash

##################################################################
#                                                                #
# Copyright (c) 2018-2023 PrimeKey Solutions AB.                 #
#                                                                #
# This software is free software; you can redistribute it and/or #
# modify it under the terms of the GNU Lesser General Public     #
# License as published by the Free Software Foundation; either   #
# version 2.1 of the License, or any later version.              #
#                                                                #
# See terms of license at gnu.org.                               #
#                                                                #
##################################################################

if [ ! -z "$DEBUG" ] ; then
    set -x
fi

# Source the os-release file to determine which OS is being used with $ID variable
source /etc/os-release

# Create Java version variable for handling java8 vs java11 differences
JAVA_VER=`java -version 2>&1 | grep 'version' 2>&1 | awk -F\" '{ split($2,a,"."); print a[1]"."a[2]}'`

# Redirect stderr to stdout to make output easier to consume by third party tools
exec 2>&1

baseDir="/opt/keyfactor"
tempDir="$(mktemp -d -p ${baseDir}/tmp)"
#log "INFO" "Container OS is ${ID}"      

# Import common functions like logger etc
if [ -f ${baseDir}/bin/internal/functions-common    ] ; then source ${baseDir}/bin/internal/functions-common    "${baseDir}" "${tempDir}" "${ID}" ; fi
if [ -f ${baseDir}/bin/internal/functions-appserver ] ; then source ${baseDir}/bin/internal/functions-appserver "${baseDir}" "${tempDir}" "${ID}" ; fi

# Source mounted secrets from external vault manager 
if [ -d ${baseDir}/secrets/external/vault ] ; then
    log "INFO" "External secrets from a vault detected."
    log "DEBUG" "Mounted environment secret files to source from external secret vault manager: $(ls -alh ${baseDir}/secrets/external/vault)"
    for f in ${baseDir}/secrets/external/vault/*.env ; do
        if [ -f "$f" ] ; then
            log "INFO" "Attempting to source vault secrets from: $(realpath ${f})."
            source "$f"
        fi
    done
fi

# Setup defaults for environment
if [ -f ${baseDir}/bin/internal/environment-pre       ] ; then source ${baseDir}/bin/internal/environment-pre      "${baseDir}" "${tempDir}" "${ID}" ; fi
if [ -f ${baseDir}/bin/internal/environment-app       ] ; then source ${baseDir}/bin/internal/environment-app      "${baseDir}" "${tempDir}" "${ID}" ; fi
if [ -f ${baseDir}/bin/internal/environment-defaults  ] ; then source ${baseDir}/bin/internal/environment-defaults "${baseDir}" "${tempDir}" "${ID}" ; fi
# Find a directory under '/opt' that has a readable 'environment-hsm' file
for subDirectory in /opt/* /opt/keyfactor/* ; do
    if [ -d "$subDirectory" ] ; then
        if [ -f "$subDirectory/environment-hsm"       ] ; then source "$subDirectory/environment-hsm"              "${baseDir}" "${tempDir}" "${ID}" && break ; fi
    fi
done
if [ -f ${baseDir}/bin/internal/environment-post      ] ; then source ${baseDir}/bin/internal/environment-post     "${baseDir}" "${tempDir}" "${ID}" ; fi

# Configure standalone.xml logging for the server
if [ -f "${baseDir}/appserver/standalone/configuration/logger-appserver" ] ; then
    appserver_config_loggers "${baseDir}/appserver/standalone/configuration/logger-appserver" "APPSERVER-LOGGERS" "Application Server"
fi

# Configure standalone.xml logging for the app type
if [ -f "${baseDir}/bin/internal/logger-app" ] ; then 
    appserver_config_loggers "${baseDir}/bin/internal/logger-app" "APP-LOGGERS" "${APPLICATION_NAME}"
fi

# Setup a username for the assigned user id that we run under unless it is already present
id | log "INFO" || log "INFO" "Failed to retrieve current user id. Ignoring."

if ! whoami &> /dev/null; then
  if [ -w /etc/passwd ]; then
    echo "${APPLICATION_NAME}:x:$(id -u):0:${APPLICATION_NAME} user:/opt:/sbin/nologin" >> /etc/passwd
  fi
fi

### Perform sanity checks ###

if [ "x$(java -version 2>&1 | grep -v bash)" == "x" ] ; then
  log "ERROR" "No 'java' found."
  exit 1
fi

if [ "x$BASH" == "x" ] ; then
  log "ERROR" "This is not a bash shell which is expected. Aborting."
  exit 1
fi

if [ "x${HSM_PKCS11_LIBRARY}" != "x" ] ; then
    if [ -f "${HSM_PKCS11_LIBRARY}" ] ; then
        log "INFO" "PKCS#11 library '${HSM_PKCS11_LIBRARY}' detected."
    else
        log "WARN" "PKCS#11 library '${HSM_PKCS11_LIBRARY}' specified, but not accessible."
        export HSM_PKCS11_LIBRARY=""
    fi
fi

### JVMs now have support for detection of cgroup limitations, but we still want to log assigned millicores for easy troubleshooting ###
if [ -e /sys/fs/cgroup/cpu/cpu.cfs_period_us ] && [ -e /sys/fs/cgroup/cpu/cpu.cfs_quota_us ] && [ $(cat /sys/fs/cgroup/cpu/cpu.cfs_quota_us) -ne -1 ] ; then
    cpuPeriod=$(cat /sys/fs/cgroup/cpu/cpu.cfs_period_us)
    cpuQouta=$(cat /sys/fs/cgroup/cpu/cpu.cfs_quota_us)
    coreLimit=$((cpuQouta/cpuPeriod))
    coreLimitFraction=$((1000*cpuQouta/cpuPeriod-1000*coreLimit))
    log "INFO" "Detected ${coreLimit}.${coreLimitFraction} available core(s)."
else
    cpuTotal=$(cat /proc/cpuinfo | grep ^processor | wc -l)
    log "INFO" "Detected ${cpuTotal} available core(s)."
fi

# Calculate memory available to the container
if [ -f /sys/fs/cgroup/memory/memory.limit_in_bytes ] ; then
  memCgroup=$(cat /sys/fs/cgroup/memory/memory.limit_in_bytes)
elif [ -f /sys/fs/cgroup/memory.max ] ; then
  memCgroup=$(cat /sys/fs/cgroup/memory.max)
else
  log "ERROR" "Expected cgroup file not found. Aborting startup."
  exit 1
fi
memTotal=$(cat /proc/meminfo | awk '/MemTotal/ {print $2*1024}')
if [ $memCgroup != -1 ] && [ $memCgroup != "max" ] && [ $memCgroup -lt ${memTotal} ] ; then
  memLimit=${memCgroup}
  log "INFO" "Detected $memLimit bytes available memory assigned to this container."
else
  memLimit=${memTotal}
  log "INFO" "Detected $memLimit bytes available host memory."
fi

if [ -z "$JAVA_OPTS_CUSTOM" ] ; then
  memRequired=912
  #if [[ "$DATABASE_JDBC_URL" =~ ^jdbc:h2:mem:.* ]]; then
  #  # Require additional minimal amount of memory when an in memory database is specified.
  #  memRequired=$(($memRequired+192))
  #fi
  if [ $(($memLimit/1024/1024)) -lt $memRequired ] ; then
    log "WARN" "You should assign at least '${memRequired}m' to this container."
  fi
  memLimitMiB=$((memLimit/1024/1024))
  # The CLIs used during startup will allocation 80m, but before app server uses up all heap
  jvmOther=80
  # -Xss (ThreadStackSize) defaults to 1m on x64 Linux ... 200 threads at 256k ≃ 50 MiB
  # The remaining memory is split between the JVM's heap and native memory segments.
  # This is done to both fully utilize the container's memory, and to restrict the application server from encroaching
  # on memory reserved for other processes within the container.
  # Approximately 30%, capped at 2GB, is given to Metaspace, while the remaining majority is allocated to heap.
  jvmXXMaxMetaspaceSize=$((((memLimitMiB - jvmOther)) / 3))
  jvmXXMaxMetaspaceLimit=$((2*1024))
  if [ $jvmXXMaxMetaspaceSize -gt $jvmXXMaxMetaspaceLimit ] ; then
    log "INFO" "Using the upper Metaspace limit of '${jvmXXMaxMetaspaceLimit}m'."
    jvmXXMaxMetaspaceSize=$jvmXXMaxMetaspaceLimit
  fi
  jvmXMx=$((memLimitMiB-jvmXXMaxMetaspaceSize-jvmOther))
  # Maximum used memory = Xmx + MetaspaceSize + #threads*Xss
  JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Xms128m -Xmx${jvmXMx}m -Xss256k -XX:MetaspaceSize=160m -XX:MaxMetaspaceSize=${jvmXXMaxMetaspaceSize}m"
fi

# -XX:MinHeapFreeRatio=X (Grow heap when less than X% is free)
# -XX:MaxHeapFreeRatio=Y (Shrink heap when more than Y% is free).

JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -XX:+UseParallelGC -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20"

### Additional runtime tweaks to Java ###

# Just kill the process if it runs out of memory instead of stalling with massive GC
#JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -XX:OnOutOfMemoryError='kill -9 %p'"
if [ "x$DEBUG" != "xtrue" ] ; then
    JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -XX:+ExitOnOutOfMemoryError"
else
    JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -XX:+CrashOnOutOfMemoryError"
fi

# Force use of 2048-bit DH keys in order to mitigate https://weakdh.org/
JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Djdk.tls.ephemeralDHKeySize=2048"

# Enable IPv6 port binding which also catches IPv4 if IPv6 seems to be enabled
if [ -e /proc/net/if_inet6 ] ; then
    NO_IPV6_AVAILABLE=$(test -z "$(cat /proc/net/if_inet6)" && echo true || echo false)
else
    NO_IPV6_AVAILABLE="true"
fi
JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Djava.net.preferIPv4Stack=${NO_IPV6_AVAILABLE}"

### Show detailed JVM info if DEBUG environment variable is configured ###

if [ ! -z "$DEBUG" ] ; then
    # Show debug log of TLS handshakes
    JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Djavax.net.debug=ssl:handshake"

    # Enable GC diagnostics if env.DEBUG is set
    if [ "x$JAVA_VER" == "x1.8" ] ; then
        JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps"
    elif [ "x$JAVA_VER" == "x11.0" ] ; then
        JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Xlog:gc*:verbose_gc.log:time"
    fi

  # Show JVM settings based on current JVM options
  eval java "$JAVA_OPTS_CUSTOM -XX:+PrintFlagsFinal -XX:+UnlockDiagnosticVMOptions -XX:NativeMemoryTracking=summary -XX:+PrintNMTStatistics" -version
  # Enable additional logging from the app-server
  #export LOG_LEVEL_SERVER_SUBSYSTEMS=${LOG_LEVEL_SERVER_SUBSYSTEMS:-INFO}
  if [ "x$LOG_LEVEL_SERVER_SUBSYSTEMS" != "xDEBUG" ] ; then
      export LOG_LEVEL_SERVER_SUBSYSTEMS="INFO"
  fi
  log "DEBUG" "Available entropy: $(cat /proc/sys/kernel/random/entropy_avail)"
fi

# By default JBoss EAP 7.2 for the Openshift base image has this configured. It would potentially be secure to enable this automatically
# * once entropy_avail has reached a certain level since system boot
# * x bytes of /dev/random has been read to ensure that also /dev/urandom has been properly initialized
# BUT this means that SHA1PRNG will be used instead of NativePRNG in Java.
SECURE_RANDOM_SOURCE="/dev/random"
case "$JAVA_SECURITY_USE_URANDOM" in
    force )
        SECURE_RANDOM_SOURCE="/dev/./urandom"
        ;;
    true )
        # Read 256 bits of random from the blocking source to ensure the pool is properly initialized
        log "INFO" "Reading 256 bits of random from blocking source before allowing /dev/urandom to be used."
        dd if=/dev/random count=1 bs=32 2>/dev/null > /dev/null
        SECURE_RANDOM_SOURCE="/dev/./urandom"
        ;;
esac

export SECURE_RANDOM_SOURCE
JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Djava.security.egd=file:${SECURE_RANDOM_SOURCE}"

# For WF25+ runtime EMP metrics and health can be enabled
observationPaths="/health /health/ready /health/live"
if [ "x${METRICS_ENABLED}" == "xtrue" ] ; then
    observationPaths="${observationPaths} /metrics"
    JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Dwildfly.statistics-enabled=true"
fi
log "INFO" "Observable at ${OBSERVABLE_BIND}:8090 under paths: ${observationPaths}"

export JAVA_OPTS_CUSTOM

### Prepare for application startup ###

# Process config for logging to mounted storage
if [ ! -z "$LOG_STORAGE_LOCATION" ] ; then
    touch "$LOG_STORAGE_LOCATION/canwrite" && rm "$LOG_STORAGE_LOCATION/canwrite"
    if [ ! $? ] ; then
        log "ERROR" "Unable to write to file '$LOG_STORAGE_LOCATION/canwrite'. Logging cannot be enabled. Aborting startup."
        exit 1
    fi
    if [ ${LOG_STORAGE_MAX_SIZE_MB:-0} -lt 2 ] ; then
        log "ERROR" "LOG_STORAGE_MAX_SIZE_MB is set to '${LOG_STORAGE_MAX_SIZE_MB}'. Logging cannot be enabled. Aborting startup."
        exit 1
    fi
    export LOG_STORAGE_ENABLED="true"
    JAVA_OPTS_CUSTOM="$JAVA_OPTS_CUSTOM -Djboss.server.log.dir=$LOG_STORAGE_LOCATION"
    if [ ${LOG_STORAGE_MAX_SIZE_MB} -lt 128 ] ; then
        export LOG_STORAGE_ROTATE_SIZE_MB=$((LOG_STORAGE_MAX_SIZE_MB/2))
    else
        export LOG_STORAGE_ROTATE_SIZE_MB=64
    fi
    export LOG_STORAGE_MAX_BACKUP_INDEX=$(( (LOG_STORAGE_MAX_SIZE_MB/LOG_STORAGE_ROTATE_SIZE_MB)-1 ))
    log "INFO" "Rotating application logs will be written to '$LOG_STORAGE_LOCATION' and use up to $(( LOG_STORAGE_ROTATE_SIZE_MB*(LOG_STORAGE_MAX_BACKUP_INDEX+1) )) MB storage."
fi

# Set a unique transaction node identifier of max 23 bytes to be used by the application server
export TRANSACTION_NODE_ID="$(echo ${HOSTNAME:-localhost} | sha256sum | cut -c1-23)"

appserver_reset_config

if [ -f ${baseDir}/bin/internal/after-init-pre.sh  ] ; then . ${baseDir}/bin/internal/after-init-pre.sh  "${baseDir}" "${tempDir}" "${ID}" ; fi
if [ -f ${baseDir}/bin/internal/after-init.sh      ] ; then . ${baseDir}/bin/internal/after-init.sh      "${baseDir}" "${tempDir}" "${ID}" ; fi
if [ -f ${baseDir}/bin/internal/after-init-post.sh ] ; then . ${baseDir}/bin/internal/after-init-post.sh "${baseDir}" "${tempDir}" "${ID}" ; fi

### Start up the application and monitor progress in a separate thread ###

appserver_prepare_startup

function sigterm_graceful_shutdown() {
    local appserverTimeout=$1
    local delayBeforeShutdown=$2
    # Let the app know that we are about to shutdown
    if [ -f ${baseDir}/bin/internal/before-shutdown.sh  ] ; then . ${baseDir}/bin/internal/before-shutdown.sh  "${baseDir}" "${tempDir}" "${ID}" ; fi
    if [ $delayBeforeShutdown -gt 0 ] ; then
        log "INFO" "Caught signal. Will initiate graceful shutdown in ${delayBeforeShutdown} seconds."
        sleep "${delayBeforeShutdown}"
        appserverTimeout=$((appserverTimeout-delayBeforeShutdown))
    fi
    appserverTimeout=$((appserverTimeout-1))
    if [ $appserverTimeout -gt 0 ] ; then
        log "INFO" "Application server ordered to try a graceful shutdown for ${appserverTimeout} second before forcefully terminating ongoing connections."
    fi
    appserver_shutdown "${appserverTimeout}"
    #log "INFO" "Shutdown sent."
    #appserver_kill
    #exit
}
trap "sigterm_graceful_shutdown ${GRACE_PERIOD_TIMEOUT_SECONDS} ${GRACE_PERIOD_SPENT_DELAYED_SECONDS}" TERM
trap "sigterm_graceful_shutdown ${GRACE_PERIOD_TIMEOUT_SECONDS} 0" INT

deployment_monitor_thread() {
    sleep 5
    deploymentDone=0
    while true ; do
        if appserver_deployment_failed ; then
            appserver_shutdown 3
            if [ "x${APPSERVER_FORCED_SHUTDOWN}" == "xtrue" ] ; then
                appserver_kill
            fi
            deploymentDone=1
        fi
        if appserver_deployment_success ;  then
            log "INFO" "Application ${f} successfully started."
            ### Call post startup hooks ###
            if [ "x$DEBUG" == "xfull" ] ; then
                appserver_filter_request_dumper_enable
            fi
            if [ -f ${baseDir}/bin/internal/after-deployed-pre.sh  ] ; then . ${baseDir}/bin/internal/after-deployed-pre.sh  "${baseDir}" "${tempDir}" "${ID}" ; fi
            if [ -f ${baseDir}/bin/internal/after-deployed.sh      ] ; then . ${baseDir}/bin/internal/after-deployed.sh      "${baseDir}" "${tempDir}" "${ID}" ; fi
            if [ -f ${baseDir}/bin/internal/after-deployed-post.sh ] ; then . ${baseDir}/bin/internal/after-deployed-post.sh "${baseDir}" "${tempDir}" "${ID}" ; fi
            deploymentDone=1
			
			EJBCA_CLI=/opt/keyfactor/bin/ejbca.sh

            $EJBCA_CLI config cmp addalias --alias CMP1
            $EJBCA_CLI  config cmp updatealias --alias CMP1 --key operationmode --value ra
            $EJBCA_CLI config cmp updatealias --alias CMP1 --key authenticationparameters --value 03865b022fc244a6383c9552f466b674
            $EJBCA_CLI  config cmp updatealias --alias CMP1 --key authenticationmodule --value HMAC
            $EJBCA_CLI  config cmp updatealias --alias CMP1 --key allowraverifypopo --value true
            
            echo "Finished setting up CMP"
            if [ "x${SHUTDOWN_AFTER_DEPLOY}" == "xtrue" ] ; then
                if [ -f ${baseDir}/bin/internal/before-shutdown.sh  ] ; then . ${baseDir}/bin/internal/before-shutdown.sh  "${baseDir}" "${tempDir}" "${ID}" ; fi
                appserver_shutdown 30
                if [ "x${APPSERVER_FORCED_SHUTDOWN}" == "xtrue" ] ; then
                    appserver_kill
                fi
            fi
        fi
        if [ $deploymentDone = 1 ] ; then
            break
        fi
        sleep 2
    done
}

(deployment_monitor_thread || log "ERROR" "Deployment monitor failed." ; ) &
deploymentMonitorThreadPid=$!

log "INFO" "Starting application server:"

appserver_start
exitValue=$?

log "INFO" "Stopped application server."

rm -rf "${tempDir}"

kill $deploymentMonitorThreadPid >/dev/null 2>&1

exit $exitValue
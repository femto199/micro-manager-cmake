find_package(Java REQUIRED)
include(UseJava)

file(
    GLOB_RECURSE
    external_jars
    ${3RD_PARY_PUBLIC_PATH}/classext/*.jar
)

get_property(MMCoreJ_JAR_PATH TARGET MMCoreJ PROPERTY JAR_FILE)
get_property(MMJ__JAR_PATH TARGET MMJ_ PROPERTY JAR_FILE)

# TODO: Add ZippyFocus build
# (Clojure source code, see acqEngine CMakeLists example)

# Autofocus
add_jar(MMAutofocus
        SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/Autofocus.java

        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     ${MMCoreJ_JAR_PATH}
                     ${MMJ__JAR_PATH})

install_jar(MMAutofocus ./mmautofocus
            COMPONENT mmautofocus)

# Autofocus Duo
add_jar(MMAutofocusDuo
        SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/AutofocusDuo.java

        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     ${MMCoreJ_JAR_PATH}
                     ${MMJ__JAR_PATH})

install_jar(MMAutofocusDuo ./mmautofocus
            COMPONENT mmautofocus)

# Autofocus TB
add_jar(MMAutofocusTB
        SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/AutofocusTB.java

        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     ${MMCoreJ_JAR_PATH}
                     ${MMJ__JAR_PATH})

install_jar(MMAutofocusTB ./mmautofocus
            COMPONENT mmautofocus)

# Oughta Focus
add_jar(MMOughtaFocus
        SOURCES ${CMAKE_CURRENT_SOURCE_DIR}/OughtaFocus.java

        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     ${MMCoreJ_JAR_PATH}
                     ${MMJ__JAR_PATH})

install_jar(MMOughtaFocus ./mmautofocus
            COMPONENT mmautofocus)

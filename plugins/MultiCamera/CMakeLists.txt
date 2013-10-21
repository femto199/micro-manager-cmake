find_package(Java REQUIRED)
include(UseJava)

file(
    GLOB_RECURSE
    external_jars
    ${3RD_PARY_PUBLIC_PATH}/classext/*.jar
)

file(
    GLOB_RECURSE
    source_files
    ./src/*.java
)

add_jar(MultiCamera
        SOURCES ${source_files}
        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     MMCoreJ
                     MMJ_)

install_jar(MultiCamera ${IJ_PATH}/mmplugins
            COMPONENT mmplugins)

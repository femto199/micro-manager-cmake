find_package(Java REQUIRED)
include(UseJava)

file(
    GLOB_RECURSE
    external_jars
    ${3RD_PARY_PUBLIC_PATH}/classext/*.jar
)

file(
    GLOB_RECURSE
    MMCoreJ_source_files
    ${CMAKE_SOURCE_DIR}/MMCoreJ_wrap/mmcorej/*.java
)

file(
    GLOB_RECURSE
    source_files
    ./src/*.java
)

add_jar(MMJ_
        SOURCES ${source_files}
                ${MMCoreJ_source_files}
                ${CMAKE_SOURCE_DIR}/mmstudio/bin/plugins_mmstudio.config
        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar)

install_jar(MMJ_ ${IJ_PATH}/plugins/Micro-Manager
            COMPONENT mmstudio)
install(FILES ${external_jars}
        DESTINATION ${IJ_PATH}/plugins
        COMPONENT mmstudio)

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

file(
    GLOB_RECURSE
    icon_files
    ./src/*.png
)

get_property(MMCoreJ_JAR_PATH TARGET MMCoreJ PROPERTY JAR_FILE)

add_jar(MMJ_
        SOURCES ${source_files}
                ${icon_files}

        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     ${MMCoreJ_JAR_PATH})

# install_jar(MMJ_ ${IJ_PATH}/plugins/Micro-Manager
#             COMPONENT mmstudio)
install(FILES ${external_jars}
        DESTINATION ${IJ_PATH}/plugins
        COMPONENT mmstudio)

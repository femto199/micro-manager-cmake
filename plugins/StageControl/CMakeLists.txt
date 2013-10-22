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

# Find a way to copy resource files to jar
file(
    GLOB icons_path
    RELATIVE ${CMAKE_CURRENT_SOURCE_DIR}
    ./src/org/micromanager/stagecontrol/icons/*
)

add_jar(StageControl
        SOURCES ${source_files}
        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     MMCoreJ
                     MMJ_)

install_jar(StageControl ./mmplugins
            COMPONENT mmplugins)

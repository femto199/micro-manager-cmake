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
    ./src/org/micromanager/icons/*
)

get_property(MMCoreJ_JAR_PATH TARGET MMCoreJ PROPERTY JAR_FILE)

set(CMAKE_JAVA_CLASS_OUTPUT_PATH "${CMAKE_CURRENT_BINARY_DIR}${CMAKE_FILES_DIRECTORY}/MMJ_.dir")

# # Copy icon files
# foreach(icon_file ${icon_files})
#     get_filename_component(icon_file ${icon_file} ABSOLUTE)
#     file(RELATIVE_PATH rel_icon_file "${CMAKE_CURRENT_SOURCE_DIR}/src" ${icon_file})
#     add_custom_command(TARGET copy_icons
#                        COMMAND cmake -E copy_if_different
#                        ARGS    ${icon_file}
#                                ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${rel_icon_file}
#                        DEPENDS ${icon_file}
#                        COMMENT "${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${rel_icon_file}"
#     )
#     list(APPEND icons_files_dest "${rel_icon_file}")
# endforeach()

# add_custom_target(copy_icons ALL DEPENDS ${icons_files_dest})

add_jar(MMJ_
        SOURCES ${source_files}
                ${icon_files}

        INCLUDE_JARS ${external_jars}
                     ${IJ_PATH}/ij.jar
                     ${MMCoreJ_JAR_PATH})

# add_dependencies(MMJ_ copy_icons)

install_jar(MMJ_ ./plugins/Micro-Manager
            COMPONENT mmstudio)

#=============================================================================
# Copyright 2010-2011 Andreas schneider <asn@redhat.com>
# Copyright 2010 Ben Boeckel <ben.boeckel@kitware.com>
#
# Distributed under the OSI-approved BSD License (the "License");
# see accompanying file Copyright.txt for details.
#
# This software is distributed WITHOUT ANY WARRANTY; without even the
# implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
# See the License for more information.
#=============================================================================
# (To distribute this file outside of CMake, substitute the full
#  License text for the above reference.)

include(${CMAKE_CURRENT_LIST_DIR}/CMakeParseArguments.cmake)

function (__java_copy_file src dest comment)
    add_custom_command(
        OUTPUT  ${dest}
        COMMAND cmake -E copy_if_different
        ARGS    ${src}
                ${dest}
        DEPENDS ${src}
        COMMENT ${comment})
endfunction ()

# define helper scripts
set(_JAVA_CLASS_FILELIST_SCRIPT ${CMAKE_CURRENT_LIST_DIR}/UseJavaClassFilelist.cmake)
set(_JAVA_SYMLINK_SCRIPT ${CMAKE_CURRENT_LIST_DIR}/UseJavaSymlinks.cmake)

function(add_jar _TARGET_NAME)

    cmake_parse_arguments(_add_jar "" "" "SOURCES;INCLUDE_JARS" ${ARGN})

    set(_JAVA_SOURCE_FILES ${_add_jar_SOURCES} ${_add_jar_UNPARSED_ARGUMENTS})

    if (NOT DEFINED CMAKE_JAVA_TARGET_OUTPUT_DIR)
      set(CMAKE_JAVA_TARGET_OUTPUT_DIR ${CMAKE_CURRENT_BINARY_DIR})
    endif()

    if (CMAKE_JAVA_JAR_ENTRY_POINT)
      set(_ENTRY_POINT_OPTION e)
      set(_ENTRY_POINT_VALUE ${CMAKE_JAVA_JAR_ENTRY_POINT})
    endif ()

    if (LIBRARY_OUTPUT_PATH)
        set(CMAKE_JAVA_LIBRARY_OUTPUT_PATH ${LIBRARY_OUTPUT_PATH})
    else ()
        set(CMAKE_JAVA_LIBRARY_OUTPUT_PATH ${CMAKE_JAVA_TARGET_OUTPUT_DIR})
    endif ()

    set(CMAKE_JAVA_INCLUDE_PATH
        ${CMAKE_JAVA_INCLUDE_PATH}
        ${CMAKE_CURRENT_SOURCE_DIR}
        ${CMAKE_JAVA_OBJECT_OUTPUT_PATH}
        ${CMAKE_JAVA_LIBRARY_OUTPUT_PATH}
    )

    if (WIN32 AND NOT CYGWIN)
        set(CMAKE_JAVA_INCLUDE_FLAG_SEP ";")
    else ()
        set(CMAKE_JAVA_INCLUDE_FLAG_SEP ":")
    endif()

    foreach (JAVA_INCLUDE_DIR ${CMAKE_JAVA_INCLUDE_PATH})
       set(CMAKE_JAVA_INCLUDE_PATH_FINAL "${CMAKE_JAVA_INCLUDE_PATH_FINAL}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${JAVA_INCLUDE_DIR}")
    endforeach()

    set(CMAKE_JAVA_CLASS_OUTPUT_PATH "${CMAKE_JAVA_TARGET_OUTPUT_DIR}${CMAKE_FILES_DIRECTORY}/${_TARGET_NAME}.dir")

    set(_JAVA_TARGET_OUTPUT_NAME "${_TARGET_NAME}.jar")
    if (CMAKE_JAVA_TARGET_OUTPUT_NAME AND CMAKE_JAVA_TARGET_VERSION)
        set(_JAVA_TARGET_OUTPUT_NAME "${CMAKE_JAVA_TARGET_OUTPUT_NAME}-${CMAKE_JAVA_TARGET_VERSION}.jar")
        set(_JAVA_TARGET_OUTPUT_LINK "${CMAKE_JAVA_TARGET_OUTPUT_NAME}.jar")
    elseif (CMAKE_JAVA_TARGET_VERSION)
        set(_JAVA_TARGET_OUTPUT_NAME "${_TARGET_NAME}-${CMAKE_JAVA_TARGET_VERSION}.jar")
        set(_JAVA_TARGET_OUTPUT_LINK "${_TARGET_NAME}.jar")
    elseif (CMAKE_JAVA_TARGET_OUTPUT_NAME)
        set(_JAVA_TARGET_OUTPUT_NAME "${CMAKE_JAVA_TARGET_OUTPUT_NAME}.jar")
    endif ()
    # reset
    set(CMAKE_JAVA_TARGET_OUTPUT_NAME)

    set(_JAVA_CLASS_FILES)
    set(_JAVA_COMPILE_FILES)
    set(_JAVA_DEPENDS)
    set(_JAVA_COMPILE_DEPENDS)
    set(_JAVA_RESOURCE_FILES)
 set(_JAVA_RESOURCE_FILES_RELATIVE)
    foreach(_JAVA_SOURCE_FILE ${_JAVA_SOURCE_FILES})
        get_filename_component(_JAVA_EXT ${_JAVA_SOURCE_FILE} EXT)
        get_filename_component(_JAVA_FILE ${_JAVA_SOURCE_FILE} NAME_WE)
        get_filename_component(_JAVA_PATH ${_JAVA_SOURCE_FILE} PATH)
        get_filename_component(_JAVA_FULL ${_JAVA_SOURCE_FILE} ABSOLUTE)

        if (_JAVA_EXT MATCHES ".java")
            file(RELATIVE_PATH _JAVA_REL_BINARY_PATH ${CMAKE_JAVA_TARGET_OUTPUT_DIR} ${_JAVA_FULL})
            file(RELATIVE_PATH _JAVA_REL_SOURCE_PATH ${CMAKE_CURRENT_SOURCE_DIR} ${_JAVA_FULL})
            string(LENGTH ${_JAVA_REL_BINARY_PATH} _BIN_LEN)
            string(LENGTH ${_JAVA_REL_SOURCE_PATH} _SRC_LEN)
            if (${_BIN_LEN} LESS ${_SRC_LEN})
                set(_JAVA_REL_PATH ${_JAVA_REL_BINARY_PATH})
            else ()
                set(_JAVA_REL_PATH ${_JAVA_REL_SOURCE_PATH})
            endif ()
            get_filename_component(_JAVA_REL_PATH ${_JAVA_REL_PATH} PATH)

            list(APPEND _JAVA_COMPILE_FILES ${_JAVA_SOURCE_FILE})
            set(_JAVA_CLASS_FILE "${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${_JAVA_REL_PATH}/${_JAVA_FILE}.class")
            set(_JAVA_CLASS_FILES ${_JAVA_CLASS_FILES} ${_JAVA_CLASS_FILE})

        elseif (_JAVA_EXT MATCHES ".jar"
                OR _JAVA_EXT MATCHES ".war"
                OR _JAVA_EXT MATCHES ".ear"
                OR _JAVA_EXT MATCHES ".sar")
            # Ignored for backward compatibility

        elseif (_JAVA_EXT STREQUAL "")
            list(APPEND CMAKE_JAVA_INCLUDE_PATH ${JAVA_JAR_TARGET_${_JAVA_SOURCE_FILE}} ${JAVA_JAR_TARGET_${_JAVA_SOURCE_FILE}_CLASSPATH})
            list(APPEND _JAVA_DEPENDS ${JAVA_JAR_TARGET_${_JAVA_SOURCE_FILE}})

        else ()
            # message(${_JAVA_SOURCE_FILE})
            #file(RELATIVE_PATH _JAVA_SOURCE_FILE ${CMAKE_CURRENT_SOURCE_DIR} ${_JAVA_SOURCE_FILE})
            # message(${CMAKE_JAVA_CLASS_OUTPUT_PATH})
            __java_copy_file(${CMAKE_CURRENT_SOURCE_DIR}/${_JAVA_SOURCE_FILE}
                             ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/${_JAVA_SOURCE_FILE}
                             "Copying ${_JAVA_SOURCE_FILE} to the build directory")
            list(APPEND _JAVA_RESOURCE_FILES ${_JAVA_SOURCE_FILE})
        endif ()
    endforeach()

    foreach(_JAVA_INCLUDE_JAR ${_add_jar_INCLUDE_JARS})
        if (TARGET ${_JAVA_INCLUDE_JAR})
            get_target_property(_JAVA_JAR_PATH ${_JAVA_INCLUDE_JAR} JAR_FILE)
            if (_JAVA_JAR_PATH)
                set(CMAKE_JAVA_INCLUDE_PATH_FINAL "${CMAKE_JAVA_INCLUDE_PATH_FINAL}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${_JAVA_JAR_PATH}")
                list(APPEND CMAKE_JAVA_INCLUDE_PATH ${_JAVA_JAR_PATH})
                list(APPEND _JAVA_DEPENDS ${_JAVA_INCLUDE_JAR})
                list(APPEND _JAVA_COMPILE_DEPENDS ${_JAVA_INCLUDE_JAR})
            else ()
                message(SEND_ERROR "add_jar: INCLUDE_JARS target ${_JAVA_INCLUDE_JAR} is not a jar")
            endif ()
        else ()
            set(CMAKE_JAVA_INCLUDE_PATH_FINAL "${CMAKE_JAVA_INCLUDE_PATH_FINAL}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${_JAVA_INCLUDE_JAR}")
            list(APPEND CMAKE_JAVA_INCLUDE_PATH "${_JAVA_INCLUDE_JAR}")
            list(APPEND _JAVA_DEPENDS "${_JAVA_INCLUDE_JAR}")
            list(APPEND _JAVA_COMPILE_DEPENDS "${_JAVA_INCLUDE_JAR}")
        endif ()
    endforeach()

    # create an empty java_class_filelist
    if (NOT EXISTS ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist)
        file(WRITE ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist "")
    endif()

    if (_JAVA_COMPILE_FILES)
        # Compile the java files and create a list of class files
        add_custom_command(
            # NOTE: this command generates an artificial dependency file
            OUTPUT ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_compiled_${_TARGET_NAME}
            COMMAND ${Java_JAVAC_EXECUTABLE}
                ${CMAKE_JAVA_COMPILE_FLAGS}
                -classpath "${CMAKE_JAVA_INCLUDE_PATH_FINAL}"
                -d ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
                ${_JAVA_COMPILE_FILES}
            COMMAND ${CMAKE_COMMAND} -E touch ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_compiled_${_TARGET_NAME}
            DEPENDS ${_JAVA_COMPILE_FILES} ${_JAVA_COMPILE_DEPENDS}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
            COMMENT "Building Java objects for ${_TARGET_NAME}.jar"
        )
        add_custom_command(
            OUTPUT ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist
            COMMAND ${CMAKE_COMMAND}
                -DCMAKE_JAVA_CLASS_OUTPUT_PATH=${CMAKE_JAVA_CLASS_OUTPUT_PATH}
                -DCMAKE_JAR_CLASSES_PREFIX="${CMAKE_JAR_CLASSES_PREFIX}"
                -P ${_JAVA_CLASS_FILELIST_SCRIPT}
            DEPENDS ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_compiled_${_TARGET_NAME}
            WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
        )
    endif ()

    # create the jar file
    set(_JAVA_JAR_OUTPUT_PATH
      ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_JAVA_TARGET_OUTPUT_NAME})
    if (CMAKE_JNI_TARGET)
        add_custom_command(
            OUTPUT ${_JAVA_JAR_OUTPUT_PATH}
            COMMAND ${Java_JAR_EXECUTABLE}
                -cf${_ENTRY_POINT_OPTION} ${_JAVA_JAR_OUTPUT_PATH} ${_ENTRY_POINT_VALUE}
                ${_JAVA_RESOURCE_FILES} @java_class_filelist
            COMMAND ${CMAKE_COMMAND}
                -D_JAVA_TARGET_DIR=${CMAKE_JAVA_TARGET_OUTPUT_DIR}
                -D_JAVA_TARGET_OUTPUT_NAME=${_JAVA_TARGET_OUTPUT_NAME}
                -D_JAVA_TARGET_OUTPUT_LINK=${_JAVA_TARGET_OUTPUT_LINK}
                -P ${_JAVA_SYMLINK_SCRIPT}
            COMMAND ${CMAKE_COMMAND}
                -D_JAVA_TARGET_DIR=${CMAKE_JAVA_TARGET_OUTPUT_DIR}
                -D_JAVA_TARGET_OUTPUT_NAME=${_JAVA_JAR_OUTPUT_PATH}
                -D_JAVA_TARGET_OUTPUT_LINK=${_JAVA_TARGET_OUTPUT_LINK}
                -P ${_JAVA_SYMLINK_SCRIPT}
            DEPENDS ${_JAVA_RESOURCE_FILES} ${_JAVA_DEPENDS} ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist
            WORKING_DIRECTORY ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
            COMMENT "Creating Java archive ${_JAVA_TARGET_OUTPUT_NAME}"
        )
    else ()
        add_custom_command(
            OUTPUT ${_JAVA_JAR_OUTPUT_PATH}
            COMMAND ${Java_JAR_EXECUTABLE}
                -cf${_ENTRY_POINT_OPTION} ${_JAVA_JAR_OUTPUT_PATH} ${_ENTRY_POINT_VALUE}
                ${_JAVA_RESOURCE_FILES} @java_class_filelist
            COMMAND ${CMAKE_COMMAND}
                -D_JAVA_TARGET_DIR=${CMAKE_JAVA_TARGET_OUTPUT_DIR}
                -D_JAVA_TARGET_OUTPUT_NAME=${_JAVA_TARGET_OUTPUT_NAME}
                -D_JAVA_TARGET_OUTPUT_LINK=${_JAVA_TARGET_OUTPUT_LINK}
                -P ${_JAVA_SYMLINK_SCRIPT}
            WORKING_DIRECTORY ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
            DEPENDS ${_JAVA_RESOURCE_FILES} ${_JAVA_DEPENDS} ${CMAKE_JAVA_CLASS_OUTPUT_PATH}/java_class_filelist
            COMMENT "Creating Java archive ${_JAVA_TARGET_OUTPUT_NAME}"
        )
    endif ()

    # Add the target and make sure we have the latest resource files.
    add_custom_target(${_TARGET_NAME} ALL DEPENDS ${_JAVA_JAR_OUTPUT_PATH})

    set_property(
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            INSTALL_FILES
                ${_JAVA_JAR_OUTPUT_PATH}
    )

    if (_JAVA_TARGET_OUTPUT_LINK)
        set_property(
            TARGET
                ${_TARGET_NAME}
            PROPERTY
                INSTALL_FILES
                    ${_JAVA_JAR_OUTPUT_PATH}
                    ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_JAVA_TARGET_OUTPUT_LINK}
        )

        if (CMAKE_JNI_TARGET)
            set_property(
                TARGET
                    ${_TARGET_NAME}
                PROPERTY
                    JNI_SYMLINK
                        ${CMAKE_JAVA_TARGET_OUTPUT_DIR}/${_JAVA_TARGET_OUTPUT_LINK}
            )
        endif ()
    endif ()

    set_property(
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            JAR_FILE
                ${_JAVA_JAR_OUTPUT_PATH}
    )

    set_property(
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            CLASSDIR
                ${CMAKE_JAVA_CLASS_OUTPUT_PATH}
    )

endfunction()

function(INSTALL_JAR _TARGET_NAME _DESTINATION)
    get_property(__FILES
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            INSTALL_FILES
    )

    if (__FILES)
        install(
            FILES
                ${__FILES}
            DESTINATION
                ${_DESTINATION}
        )
    else ()
        message(SEND_ERROR "The target ${_TARGET_NAME} is not known in this scope.")
    endif ()
endfunction()

function(INSTALL_JNI_SYMLINK _TARGET_NAME _DESTINATION)
    get_property(__SYMLINK
        TARGET
            ${_TARGET_NAME}
        PROPERTY
            JNI_SYMLINK
    )

    if (__SYMLINK)
        install(
            FILES
                ${__SYMLINK}
            DESTINATION
                ${_DESTINATION}
        )
    else ()
        message(SEND_ERROR "The target ${_TARGET_NAME} is not known in this scope.")
    endif ()
endfunction()

function (find_jar VARIABLE)
    set(_jar_names)
    set(_jar_files)
    set(_jar_versions)
    set(_jar_paths
        /usr/share/java/
        /usr/local/share/java/
        ${Java_JAR_PATHS})
    set(_jar_doc "NOTSET")

    set(_state "name")

    foreach (arg ${ARGN})
        if (${_state} STREQUAL "name")
            if (${arg} STREQUAL "VERSIONS")
                set(_state "versions")
            elseif (${arg} STREQUAL "NAMES")
                set(_state "names")
            elseif (${arg} STREQUAL "PATHS")
                set(_state "paths")
            elseif (${arg} STREQUAL "DOC")
                set(_state "doc")
            else ()
                set(_jar_names ${arg})
                if (_jar_doc STREQUAL "NOTSET")
                    set(_jar_doc "Finding ${arg} jar")
                endif ()
            endif ()
        elseif (${_state} STREQUAL "versions")
            if (${arg} STREQUAL "NAMES")
                set(_state "names")
            elseif (${arg} STREQUAL "PATHS")
                set(_state "paths")
            elseif (${arg} STREQUAL "DOC")
                set(_state "doc")
            else ()
                set(_jar_versions ${_jar_versions} ${arg})
            endif ()
        elseif (${_state} STREQUAL "names")
            if (${arg} STREQUAL "VERSIONS")
                set(_state "versions")
            elseif (${arg} STREQUAL "PATHS")
                set(_state "paths")
            elseif (${arg} STREQUAL "DOC")
                set(_state "doc")
            else ()
                set(_jar_names ${_jar_names} ${arg})
                if (_jar_doc STREQUAL "NOTSET")
                    set(_jar_doc "Finding ${arg} jar")
                endif ()
            endif ()
        elseif (${_state} STREQUAL "paths")
            if (${arg} STREQUAL "VERSIONS")
                set(_state "versions")
            elseif (${arg} STREQUAL "NAMES")
                set(_state "names")
            elseif (${arg} STREQUAL "DOC")
                set(_state "doc")
            else ()
                set(_jar_paths ${_jar_paths} ${arg})
            endif ()
        elseif (${_state} STREQUAL "doc")
            if (${arg} STREQUAL "VERSIONS")
                set(_state "versions")
            elseif (${arg} STREQUAL "NAMES")
                set(_state "names")
            elseif (${arg} STREQUAL "PATHS")
                set(_state "paths")
            else ()
                set(_jar_doc ${arg})
            endif ()
        endif ()
    endforeach ()

    if (NOT _jar_names)
        message(FATAL_ERROR "find_jar: No name to search for given")
    endif ()

    foreach (jar_name ${_jar_names})
        foreach (version ${_jar_versions})
            set(_jar_files ${_jar_files} ${jar_name}-${version}.jar)
        endforeach ()
        set(_jar_files ${_jar_files} ${jar_name}.jar)
    endforeach ()

    find_file(${VARIABLE}
        NAMES   ${_jar_files}
        PATHS   ${_jar_paths}
        DOC     ${_jar_doc}
        NO_DEFAULT_PATH)
endfunction ()

function(create_javadoc _target)
    set(_javadoc_packages)
    set(_javadoc_files)
    set(_javadoc_sourcepath)
    set(_javadoc_classpath)
    set(_javadoc_installpath "${CMAKE_INSTALL_PREFIX}/share/javadoc")
    set(_javadoc_doctitle)
    set(_javadoc_windowtitle)
    set(_javadoc_author FALSE)
    set(_javadoc_version FALSE)
    set(_javadoc_use FALSE)

    set(_state "package")

    foreach (arg ${ARGN})
        if (${_state} STREQUAL "package")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_packages ${arg})
                set(_state "packages")
            endif ()
        elseif (${_state} STREQUAL "packages")
            if (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                list(APPEND _javadoc_packages ${arg})
            endif ()
        elseif (${_state} STREQUAL "files")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                list(APPEND _javadoc_files ${arg})
            endif ()
        elseif (${_state} STREQUAL "sourcepath")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                list(APPEND _javadoc_sourcepath ${arg})
            endif ()
        elseif (${_state} STREQUAL "classpath")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                list(APPEND _javadoc_classpath ${arg})
            endif ()
        elseif (${_state} STREQUAL "installpath")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_installpath ${arg})
            endif ()
        elseif (${_state} STREQUAL "doctitle")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_doctitle ${arg})
            endif ()
        elseif (${_state} STREQUAL "windowtitle")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_windowtitle ${arg})
            endif ()
        elseif (${_state} STREQUAL "author")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_author ${arg})
            endif ()
        elseif (${_state} STREQUAL "use")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_use ${arg})
            endif ()
        elseif (${_state} STREQUAL "version")
            if (${arg} STREQUAL "PACKAGES")
                set(_state "packages")
            elseif (${arg} STREQUAL "FILES")
                set(_state "files")
            elseif (${arg} STREQUAL "SOURCEPATH")
                set(_state "sourcepath")
            elseif (${arg} STREQUAL "CLASSPATH")
                set(_state "classpath")
            elseif (${arg} STREQUAL "INSTALLPATH")
                set(_state "installpath")
            elseif (${arg} STREQUAL "DOCTITLE")
                set(_state "doctitle")
            elseif (${arg} STREQUAL "WINDOWTITLE")
                set(_state "windowtitle")
            elseif (${arg} STREQUAL "AUTHOR")
                set(_state "author")
            elseif (${arg} STREQUAL "USE")
                set(_state "use")
            elseif (${arg} STREQUAL "VERSION")
                set(_state "version")
            else ()
                set(_javadoc_version ${arg})
            endif ()
        endif ()
    endforeach ()

    set(_javadoc_builddir ${CMAKE_CURRENT_BINARY_DIR}/javadoc/${_target})
    set(_javadoc_options -d ${_javadoc_builddir})

    if (_javadoc_sourcepath)
        set(_start TRUE)
        foreach(_path ${_javadoc_sourcepath})
            if (_start)
                set(_sourcepath ${_path})
                set(_start FALSE)
            else ()
                set(_sourcepath ${_sourcepath}:${_path})
            endif ()
        endforeach()
        set(_javadoc_options ${_javadoc_options} -sourcepath ${_sourcepath})
    endif ()

    if (_javadoc_classpath)
        set(_start TRUE)
        foreach(_path ${_javadoc_classpath})
            if (_start)
                set(_classpath ${_path})
                set(_start FALSE)
            else ()
                set(_classpath ${_classpath}:${_path})
            endif ()
        endforeach()
        set(_javadoc_options ${_javadoc_options} -classpath "${_classpath}")
    endif ()

    if (_javadoc_doctitle)
        set(_javadoc_options ${_javadoc_options} -doctitle '${_javadoc_doctitle}')
    endif ()

    if (_javadoc_windowtitle)
        set(_javadoc_options ${_javadoc_options} -windowtitle '${_javadoc_windowtitle}')
    endif ()

    if (_javadoc_author)
        set(_javadoc_options ${_javadoc_options} -author)
    endif ()

    if (_javadoc_use)
        set(_javadoc_options ${_javadoc_options} -use)
    endif ()

    if (_javadoc_version)
        set(_javadoc_options ${_javadoc_options} -version)
    endif ()

    add_custom_target(${_target}_javadoc ALL
        COMMAND ${Java_JAVADOC_EXECUTABLE} ${_javadoc_options}
                            ${_javadoc_files}
                            ${_javadoc_packages}
        WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
    )

    install(
        DIRECTORY ${_javadoc_builddir}
        DESTINATION ${_javadoc_installpath}
    )
endfunction()

find_package(Java REQUIRED)
include(UseJava)

file(
    GLOB_RECURSE
    source_files
    ./src/*.java
)

set(JAR_NAME DataBrowser.jar)
set(PACKAGE_NAME org.micromanager.browser.plugin)
get_property(MMCoreJ_JAR_PATH TARGET MMCoreJ PROPERTY JAR_FILE)
get_property(MMJ__JAR_PATH TARGET MMJ_ PROPERTY JAR_FILE)
set(CLOJURE_JAR_PATH ${3RD_PARY_PUBLIC_PATH}/classext/clojure.jar)
set(CLOJURE_CONTRIB_JAR_PATH ${3RD_PARY_PUBLIC_PATH}/classext/clojure-contrib.jar)

set(BSH_JAR_PATH ${3RD_PARY_PUBLIC_PATH}/classext/bsh-2.0b4.jar)
set(ACQENGINE_JAR_PATH ${CMAKE_BINARY_DIR}/acqEngine/MMAcqEngine.jar)
set(JSON_JAR_PATH ${3RD_PARY_PUBLIC_PATH}/classext/data.json.jar)

set(CLOJURE_COMPILED_PATH ${CMAKE_CURRENT_BINARY_DIR}/clojure_classes)
set(CLOJURE_SOURCE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src)
set(GENERATED_JAR_PATH ${CMAKE_CURRENT_BINARY_DIR}/${JAR_NAME})

add_custom_target(
    ${JAR_NAME} ALL
    DEPENDS ${GENERATED_JAR_PATH}
)

add_custom_command(
   OUTPUT  ${GENERATED_JAR_PATH}
   COMMAND ${CMAKE_COMMAND} -E make_directory ${CLOJURE_COMPILED_PATH}
   COMMAND ${Java_JAVA_EXECUTABLE}
           -server -classpath
           .:${MMCoreJ_JAR_PATH}:${CLOJURE_JAR_PATH}:${IJ_PATH}/ij.jar:${MMJ__JAR_PATH}:${BSH_JAR_PATH}:${CLOJURE_SOURCE_PATH}:${CLOJURE_COMPILED_PATH}:${CLOJURE_CONTRIB_JAR_PATH}:${ACQENGINE_JAR_PATH}:${JSON_JAR_PATH}
           -Djava.awt.headless=true
           -Dclojure.compile.path=${CLOJURE_COMPILED_PATH}
           -server clojure.lang.Compile
           ${PACKAGE_NAME}
    COMMAND ${Java_JAR_EXECUTABLE}
            cvf
            ${GENERATED_JAR_PATH}
            -C ${CLOJURE_COMPILED_PATH}/ .
   COMMENT "Clojure compilation for ${PACKAGE_NAME}"
   WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
)

install(FILES ${GENERATED_JAR_PATH}
        DESTINATION ${IJ_PATH}/mmplugins
        COMPONENT acqEngine)

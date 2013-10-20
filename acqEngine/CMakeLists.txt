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
    ./src/*.clj
)

# list(APPEND CMAKE_JAVA_INCLUDE_PATH ${external_jars})
#add_jar(MMAcqEngine ${source_files})

# add_custom_command(
#    OUTPUT acq_engine_classes
#    COMMAND ${Java_JAVA_EXECUTABLE} -server -cp ${external_jars} -Djava.awt.headless=true -Dclojure.compile.path=src -server clojure.lang.Compile org.micromanager.acq-engine
#     DEPENDS ${source_files})
# add_custom_target(run ALL DEPENDS acq_engine_classes)

# install_jar(MMAcqEngine ${IJ_PATH}/plugins/Micro-Manager
#             COMPONENT acq_engine)


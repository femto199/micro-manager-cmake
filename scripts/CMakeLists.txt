# Installation directives (to use with make install)
file(
    GLOB_RECURSE
    script_files
    *
)
install(FILES ${script_files}
        DESTINATION ${IJ_PATH}/scripts
        COMPONENT scripts)

# Installation directives (to use with make install)
file(
    GLOB_RECURSE
    script_files
    *.bsh
)
install(FILES ${script_files}
        DESTINATION ./scripts
        COMPONENT scripts)

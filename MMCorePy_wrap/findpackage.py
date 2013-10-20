#!/usr/bin/env python

import sys

if len( sys.argv ) < 2:
	sys.exit( 1 )

package = sys.argv[ 1 ]

try:
	p = __import__( package )
	print( p.__path__[ 0 ] )
except ImportError:
	print( "NOTFOUND")
	sys.exit( 1 )

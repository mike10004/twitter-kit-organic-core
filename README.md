# twitter-kit-organic-core

Java port of some parts of the core module of the Twitter SDK for Android. 
This library does not require the Android SDK or runtime libraries. The 
motivation is that I think Twitter4J is a good library but a bit beefy, and
I wanted a library less tied to global state and whose data model more 
closely tracks the JSON responses from the Twitter API.

The version is of the format `X.Y.ZrN` where `X.Y.Z` is taken from the 
official Twitter SDK version (see https://github.com/twitter/twitter-kit-android)
and the `N` is the revision number of this port.

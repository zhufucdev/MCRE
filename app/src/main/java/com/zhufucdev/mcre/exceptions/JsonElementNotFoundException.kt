package com.zhufucdev.mcre.exceptions

class JsonElementNotFoundException(path: String, fileName: String) : Exception("$path at $fileName")
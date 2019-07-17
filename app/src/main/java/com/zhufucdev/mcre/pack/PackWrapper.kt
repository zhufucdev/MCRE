package com.zhufucdev.mcre.pack

import java.io.File

/**
 * Returned by ResourcesPack.from(file: File)
 * @param instance returns null when type refers to ResourcesPack.Type.Unless or ResourcesPack.Type.Unknown
 * @param file the root directory of the pack wrapped
 */
class PackWrapper(val type: ResourcesPack.Type, val file: File, val instance: ResourcesPack?)
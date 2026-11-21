  signingConfigs {
    create("release") {
      val keystorePath = providers.environmentVariable("KEYSTORE_PATH").orNull
      val storePasswordValue = providers.environmentVariable("STORE_PASSWORD").orNull
      val keyAliasValue = providers.environmentVariable("KEY_ALIAS").orNull
      val keyPasswordValue = providers.environmentVariable("KEY_PASSWORD").orNull

      if (keystorePath != null && storePasswordValue != null && keyAliasValue != null && keyPasswordValue != null) {
        storeFile = file(keystorePath)
        storePassword = storePasswordValue
        keyAlias = keyAliasValue
        keyPassword = keyPasswordValue
      }
    }
    create("debugConfig") {
      val debugKeystore = file("${rootDir}/debug.keystore")
      if (debugKeystore.exists()) {
        storeFile = debugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

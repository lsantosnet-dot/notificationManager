# Room, ML Kit and reflection-based libraries ship their own consumer rules.
# Keep entity/DAO classes referenced via reflection just in case.
-keep class com.leo.painelnotificacoes.data.local.** { *; }

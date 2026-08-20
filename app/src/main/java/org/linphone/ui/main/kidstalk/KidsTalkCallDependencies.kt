package org.linphone.ui.main.kidstalk

/** Minimal contact operations consumed by the single-contact fragment. */
internal interface KidsTalkContactStore {
    fun resolve(): ResolvedKidsTalkContact?

    fun loadLocalContact(): KidsTalkContact?

    fun saveLocal(contact: KidsTalkContact): Boolean
}

/** One-shot device-credential operation consumed by the single-contact fragment. */
internal interface DeviceCredentialGate {
    fun requestAuthorization()
}

/**
 * The validated internal-call operation consumed by the single-contact fragment.
 * Implementations must never normalize or start a generic external URI.
 */
internal interface KidsTalkCallAction {
    fun placeValidatedExtension(extension: String): Boolean
}

/**
 * Host-local test seam. It is installed only on a fragment instance created by an
 * instrumentation FragmentFactory; ordinary Android recreation retains the no-argument
 * fragment constructor and production dependencies.
 */
internal data class KidsTalkCallTestDependencies(
    val contactStore: KidsTalkContactStore,
    val credentialGateFactory: ((DeviceCredentialGateResult) -> Unit) -> DeviceCredentialGate,
    val callAction: KidsTalkCallAction
)

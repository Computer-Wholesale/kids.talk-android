/*
 * Kids.Talk — About / Support Bottom Sheet
 *
 * KID-270: Branded about screen accessible from the info icon on the call screen (KID-263).
 *
 * Displays:
 *   - App name and version badge
 *   - Support link  → https://www.kids.talk/support
 *   - Manage account link → https://www.kids.talk/account/manage  (Play account deletion path)
 *   - Open-source licences link → in-app OssLicensesMenuActivity (GPL-3.0 compliance, KID-268)
 *   - CLOSE button
 *
 * Brand: Neo-Brutalist — zero radius, 4px hard borders, Sunshine Yellow / Grass Green palette.
 * Privacy: no child-identifying language; uses Guardian/Household terminology.
 */
package org.linphone.ui.main.kidstalk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.linphone.BuildConfig
import org.linphone.R

class KidsTalkAboutBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "KidsTalkAboutBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_kidstalk_about, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Version badge ────────────────────────────────────────────────────
        view.findViewById<TextView>(R.id.about_version).text =
            getString(R.string.kt_about_version_label) + " " + BuildConfig.VERSION_NAME

        // ── Privacy Policy link ──────────────────────────────────────────────
        view.findViewById<TextView>(R.id.about_privacy_link).setOnClickListener {
            openUrl(getString(R.string.kt_about_privacy_url))
        }

        // ── Support link ─────────────────────────────────────────────────────
        view.findViewById<TextView>(R.id.about_support_link).setOnClickListener {
            openUrl(getString(R.string.kt_about_support_url))
        }

        // ── Manage account link (Play account deletion path) ─────────────────
        view.findViewById<TextView>(R.id.about_manage_account_link).setOnClickListener {
            openUrl(getString(R.string.kt_about_manage_account_url))
        }

        // ── Open-source licences (GPL-3.0 compliance, KID-268) ───────────────
        view.findViewById<TextView>(R.id.about_licenses_link).setOnClickListener {
            // OssLicensesMenuActivity is provided by the play-services-oss-licenses plugin.
            // If the plugin is not present, this falls back to a plain web URL.
            try {
                val cls = Class.forName(
                    "com.google.android.gms.oss.licenses.OssLicensesMenuActivity"
                )
                startActivity(Intent(requireContext(), cls))
            } catch (_: ClassNotFoundException) {
                openUrl("https://www.kids.talk/open-source-licenses")
            }
        }

        // ── CLOSE button ─────────────────────────────────────────────────────
        view.findViewById<Button>(R.id.about_close_button).setOnClickListener {
            dismiss()
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

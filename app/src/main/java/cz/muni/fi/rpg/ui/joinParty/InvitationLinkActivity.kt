package cz.muni.fi.rpg.ui.joinParty

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import cz.muni.fi.rpg.R
import cz.muni.fi.rpg.model.domain.party.Invitation
import cz.muni.fi.rpg.ui.MainActivity
import cz.muni.fi.rpg.ui.common.AuthenticationFragment
import cz.muni.fi.rpg.ui.common.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import timber.log.Timber

class InvitationLinkActivity : AppCompatActivity(R.layout.activity_invitation_link),
    CoroutineScope by CoroutineScope(Dispatchers.Default),
    JoinPartyDialog.Listener,
    AuthenticationFragment.Listener {
    private val jsonMapper: JsonMapper by inject()

    override fun onStart() {
        super.onStart()

        supportFragmentManager.commit { add(AuthenticationFragment(), "Authentication") }
    }

    override fun onAuthenticated(userId: String) {
        launch { acceptDynamicLinks(userId) }
    }

    override fun onSuccessfulPartyJoin() {
        openPartyList()
    }

    override fun onDialogDismiss() {
        openPartyList()
    }

    private suspend fun acceptDynamicLinks(userId: String) {
        try {
            val link = Firebase.dynamicLinks.getDynamicLink(intent).await()?.link ?: return
            val invitationJson = link.getQueryParameter("invitation")

            if (invitationJson == null) {
                Timber.d("Dynamic link URI does not have 'invitation' query parameter")

                withContext(Dispatchers.Main) { openPartyList() }
                return
            }

            val invitation = jsonMapper.readValue<Invitation>(invitationJson)

            withContext(Dispatchers.Main) {
                JoinPartyDialog.newInstance(userId, invitation).show(supportFragmentManager, null)
            }
        } catch (e: Throwable) {
            Timber.w(e, "Could not process Dynamic Link data")
            withContext(Dispatchers.Main) {
                toast("Invalid link")
                openPartyList()
            }
        }
    }

    private fun openPartyList() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
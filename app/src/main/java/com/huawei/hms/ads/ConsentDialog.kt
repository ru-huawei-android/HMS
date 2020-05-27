package com.huawei.hms.ads

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.huawei.hms.ads.consent.bean.AdProvider
import com.huawei.hms.ads.consent.constant.ConsentStatus
import com.huawei.hms.ads.consent.inter.Consent

class ConsentDialog(context: Context, providers: MutableList<AdProvider>): Dialog(context, R.style.dialog) {

    private var mContext: Context? = null
    private var inflater: LayoutInflater? = null
    private var contentLayout: LinearLayout? = null
    private var titleTv: TextView? = null
    private var initInfoTv: TextView? = null
    private var moreInfoTv: TextView? = null
    private var partnersListTv: TextView? = null
    private var consentDialogView: View? = null
    private var initView: View? = null
    private var moreInfoView: View? = null
    private var partnersListView: View? = null
    private var consentYesBtn: Button? = null
    private var consentNoBtn: Button? = null
    private var moreInfoBackBtn: Button? = null
    private var partnerListBackBtn: Button? = null
    private var madProviders: List<AdProvider>? = null
    private var mCallback: ConsentDialogCallback? = null

    init {
        mContext = context
        madProviders = providers
    }

    interface ConsentDialogCallback {
        fun updateConsentStatus(consentStatus: ConsentStatus?)
    }

    fun setCallback(callback: ConsentDialogCallback?) {
        mCallback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dialogWindow: Window? = window
        dialogWindow?.requestFeature(Window.FEATURE_NO_TITLE)
        inflater = LayoutInflater.from(mContext)
        consentDialogView = inflater?.inflate(R.layout.dialog_consent, null)
        setContentView(consentDialogView!!)

        initView = inflater?.inflate(R.layout.dialog_consent_content, null)
        moreInfoView = inflater?.inflate(R.layout.dialog_consent_moreinfo, null)
        partnersListView = inflater?.inflate(R.layout.dialog_consent_partner_list, null)

        titleTv = findViewById(R.id.consent_dialog_title_text)
        titleTv!!.text = mContext!!.getString(R.string.consent_title)

        showInitConsentInfo()
    }

    // сохраняем выбор пользвоателя в shared preferences
    private fun updateConsentStatus(consentStatus: ConsentStatus) {
        Consent.getInstance(mContext).setConsentStatus(consentStatus)

        val preferences = mContext!!.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
        preferences.edit().putInt(SP_CONSENT_KEY, consentStatus.value).apply()

        if (mCallback != null) {
            mCallback!!.updateConsentStatus(consentStatus)
        }
    }


    private fun showInitConsentInfo() {
        addContentView(initView)
        addInitButtonAndLinkClick(consentDialogView)
    }


    private fun addInitButtonAndLinkClick(rootView: View?) {
        consentYesBtn = rootView!!.findViewById(R.id.btn_consent_init_yes)
        (consentYesBtn as Button).setOnClickListener(View.OnClickListener {
            dismiss()
            updateConsentStatus(ConsentStatus.PERSONALIZED)
        })
        consentNoBtn = rootView.findViewById(R.id.btn_consent_init_skip)
        (consentNoBtn as Button).setOnClickListener(View.OnClickListener {
            dismiss()
            updateConsentStatus(ConsentStatus.NON_PERSONALIZED)
        })
        initInfoTv = rootView.findViewById(R.id.consent_center_init_content)
        (initInfoTv as TextView).movementMethod = ScrollingMovementMethod.getInstance()
        val initText = mContext!!.getString(R.string.consent_init_text)
        val spanInitText = SpannableStringBuilder(initText)

        val initTouchHere: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showTouchHereInfo()
            }
        }
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0000FF"))
        val initTouchHereStart = mContext!!.resources.getInteger(R.integer.init_here_start)
        val initTouchHereEnd = mContext!!.resources.getInteger(R.integer.init_here_end)
        spanInitText.setSpan(initTouchHere, initTouchHereStart, initTouchHereEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanInitText.setSpan(colorSpan, initTouchHereStart, initTouchHereEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        (initInfoTv as TextView).text = spanInitText
        (initInfoTv as TextView).movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showTouchHereInfo() {
        addContentView(moreInfoView)
        addMoreInfoButtonAndLinkClick(consentDialogView)
    }

    private fun addMoreInfoButtonAndLinkClick(rootView: View?) {
        moreInfoBackBtn = rootView!!.findViewById(R.id.btn_consent_more_info_back)
        (moreInfoBackBtn as Button).setOnClickListener(View.OnClickListener { showInitConsentInfo() })
        moreInfoTv = rootView.findViewById(R.id.consent_center_more_info_content)
        (moreInfoTv as TextView).movementMethod = ScrollingMovementMethod.getInstance()
        val moreInfoText = mContext!!.getString(R.string.consent_more_info_text)
        val spanMoreInfoText = SpannableStringBuilder(moreInfoText)
        val moreInfoTouchHere: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                showPartnersListInfo()
            }
        }
        val colorSpan = ForegroundColorSpan(Color.parseColor("#0000FF"))
        val moreInfoTouchHereStart = mContext!!.resources.getInteger(R.integer.more_info_here_start)
        val moreInfoTouchHereEnd = mContext!!.resources.getInteger(R.integer.more_info_here_end)
        spanMoreInfoText.setSpan(moreInfoTouchHere, moreInfoTouchHereStart, moreInfoTouchHereEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spanMoreInfoText.setSpan(colorSpan, moreInfoTouchHereStart, moreInfoTouchHereEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        (moreInfoTv as TextView).text = spanMoreInfoText
        (moreInfoTv as TextView).movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showPartnersListInfo() {
        partnersListTv = partnersListView!!.findViewById(R.id.partners_list_content)
        (partnersListTv as TextView).movementMethod = ScrollingMovementMethod.getInstance()
        (partnersListTv as TextView).text = ""
        val learnAdProviders = madProviders
        if (learnAdProviders != null) {
            for (learnAdProvider in learnAdProviders) {
                val link = ("<font color='#0000FF'><a href=" + learnAdProvider.privacyPolicyUrl + ">"
                        + learnAdProvider.name + "</a>")
                (partnersListTv as TextView).append(Html.fromHtml(link))
                (partnersListTv as TextView).append("  ")
            }
        } else {
            (partnersListTv as TextView).append(" 3rd party’s full list of advertisers is empty")
        }
        (partnersListTv as TextView).movementMethod = LinkMovementMethod.getInstance()
        addContentView(partnersListView)
        addPartnersListButtonAndLinkClick(consentDialogView)
    }

    private fun addPartnersListButtonAndLinkClick(rootView: View?) {
        partnerListBackBtn = rootView!!.findViewById(R.id.btn_partners_list_back)
        (partnerListBackBtn as Button).setOnClickListener(View.OnClickListener { showTouchHereInfo() })
    }

    private fun addContentView(view: View?) {
        contentLayout = findViewById(R.id.consent_center_layout)
        contentLayout!!.removeAllViews()
        contentLayout!!.addView(view)
    }
}
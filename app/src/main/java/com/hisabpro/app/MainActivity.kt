package com.hisabpro.app

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var db: LedgerDb
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout
    private val money = NumberFormat.getNumberInstance(Locale.US).apply { minimumFractionDigits = 2; maximumFractionDigits = 2 }
    private val bg = Color.rgb(10, 18, 32)
    private val panel = Color.rgb(20, 31, 50)
    private val panel2 = Color.rgb(27, 41, 64)
    private val text = Color.rgb(239, 244, 251)
    private val muted = Color.rgb(150, 164, 184)
    private val emerald = Color.rgb(36, 200, 145)
    private val gold = Color.rgb(242, 190, 70)
    private val red = Color.rgb(255, 103, 120)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = LedgerDb(this); db.seed(); buildShell(); showDashboard()
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(18), dp(18), dp(28)) }
        scroll.addView(content, ScrollView.LayoutParams(-1, -2))
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(6), dp(7), dp(6), dp(8)); setBackgroundColor(Color.rgb(14, 24, 40)) }
        addNav("Home", "⌂") { showDashboard() }; addNav("Entries", "↕") { showTransactions() }; addNav("People", "◎") { showPeople() }; addNav("Reports", "▥") { showReports() }
        root.addView(nav, LinearLayout.LayoutParams(-1, dp(72))); setContentView(root)
    }

    private fun addNav(label: String, icon: String, click: () -> Unit) {
        val b = TextView(this).apply { this.text = "$icon\n$label"; gravity = Gravity.CENTER; setTextColor(muted); textSize = 12f; setOnClickListener { click() } }
        nav.addView(b, LinearLayout.LayoutParams(0, -1, 1f))
    }

    private fun clear(title: String, subtitle: String) {
        content.removeAllViews()
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val titles = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        titles.addView(tv(title, 27f, text, true)); titles.addView(tv(subtitle, 12f, muted, false).apply { setPadding(0, dp(3), 0, 0) })
        row.addView(titles, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(TextView(this).apply { this.text = "HP"; gravity = Gravity.CENTER; textSize = 16f; setTextColor(gold); setTypeface(typeface, 1); background = pill(panel2, 18f); setPadding(dp(12), dp(9), dp(12), dp(9)) })
        content.addView(row); space(18)
    }

    private fun showDashboard() {
        clear("Hisab Pro", "Saudi-ready finance • SAR • Offline")
        val s = db.summary(); val balance = s.income - s.expense
        val hero = card(panel, 22f)
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(20), dp(20), dp(20), dp(20)) }
        box.addView(tv("TOTAL BALANCE", 11f, muted, true)); box.addView(tv("SAR ${money.format(balance)}", 31f, text, true).apply { setPadding(0, dp(6), 0, dp(14)) })
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        stats.addView(stat("Income", s.income, emerald), LinearLayout.LayoutParams(0, -2, 1f)); stats.addView(stat("Expense", s.expense, red), LinearLayout.LayoutParams(0, -2, 1f))
        box.addView(stats); hero.addView(box); content.addView(hero); space(14)
        val quick = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        quick.addView(actionCard("+ Income", "Money received", emerald) { transactionDialog("INCOME") }, LinearLayout.LayoutParams(0, dp(88), 1f).apply { marginEnd = dp(7) })
        quick.addView(actionCard("− Expense", "Money spent", red) { transactionDialog("EXPENSE") }, LinearLayout.LayoutParams(0, dp(88), 1f).apply { marginStart = dp(7) })
        content.addView(quick); space(18)
        section("Overview")
        val grid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        grid.addView(metric("Receivable", s.receivable, gold), LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(6) })
        grid.addView(metric("Payable", s.payable, red), LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(6) })
        content.addView(grid); space(18)
        section("Accounts")
        db.accounts().forEach { a ->
            val c = card(panel, 17f); val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(14), dp(16), dp(14)) }
            val left = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(tv(a.name, 15f, text, true)); addView(tv(a.type, 11f, muted, false)) }
            r.addView(left, LinearLayout.LayoutParams(0, -2, 1f)); r.addView(tv("SAR ${money.format(a.balance)}", 15f, text, true)); c.addView(r); content.addView(c); space(9)
        }
        content.addView(button("+ Add account", gold) { accountDialog() }); space(20); section("Recent activity")
        val recent = db.transactions(5)
        if (recent.isEmpty()) empty("No entries yet", "Add your first income or expense to start your ledger.") else recent.forEach { transactionRow(it) }
    }

    private fun showTransactions() {
        clear("Entries", "All income & expenses")
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(button("+ Income", emerald) { transactionDialog("INCOME") }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(6) })
        row.addView(button("− Expense", red) { transactionDialog("EXPENSE") }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(6) })
        content.addView(row); space(18)
        val all = db.transactions(200)
        if (all.isEmpty()) empty("No transactions", "Your ledger entries will appear here.") else all.forEach { transactionRow(it) }
    }

    private fun transactionRow(t: Txn) {
        val c = card(panel, 16f); val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(15), dp(13), dp(15), dp(13)) }
        val signColor = if (t.type == "INCOME") emerald else red
        val badge = TextView(this).apply { this.text = if (t.type == "INCOME") "+" else "−"; gravity = Gravity.CENTER; textSize = 20f; setTextColor(signColor); background = pill(panel2, 14f) }
        r.addView(badge, LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(12) })
        val mid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(tv(t.category, 14f, text, true)); addView(tv("${t.account} • ${t.date}${if(t.note.isNotBlank()) " • ${t.note}" else ""}", 10f, muted, false)) }
        r.addView(mid, LinearLayout.LayoutParams(0, -2, 1f)); r.addView(tv("${if(t.type=="INCOME") "+" else "−"} SAR ${money.format(t.amount)}", 13f, signColor, true))
        c.addView(r); content.addView(c); space(8)
    }

    private fun showPeople() {
        clear("People", "Customer, supplier & personal ledgers"); content.addView(button("+ Add person / ledger", gold) { personDialog() }); space(16)
        val list = db.people()
        if (list.isEmpty()) empty("No people ledgers", "Track money you will receive or need to pay.")
        list.forEach { p ->
            val c = card(panel, 17f); c.setOnClickListener { personEntryDialog(p) }
            val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(16), dp(15), dp(16), dp(15)) }
            val av = TextView(this).apply { this.text = p.name.take(1).uppercase(); gravity = Gravity.CENTER; textSize = 18f; setTextColor(gold); background = pill(panel2, 18f) }
            r.addView(av, LinearLayout.LayoutParams(dp(46), dp(46)).apply { marginEnd = dp(12) })
            val mid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(tv(p.name, 15f, text, true)); addView(tv(p.phone.ifBlank { "Tap to add ledger entry" }, 11f, muted, false)) }
            r.addView(mid, LinearLayout.LayoutParams(0, -2, 1f)); val bal = db.personBalance(p.id); val col = if (bal >= 0) emerald else red
            r.addView(tv("${if(bal>=0) "Receive" else "Pay"}\nSAR ${money.format(kotlin.math.abs(bal))}", 12f, col, true).apply { gravity = Gravity.END })
            c.addView(r); content.addView(c); space(9)
        }
    }

    private fun showReports() {
        clear("Reports", "Insights, totals & backup"); val s = db.summary(); val month = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
        section(month); content.addView(metric("Total income", s.income, emerald)); space(8); content.addView(metric("Total expense", s.expense, red)); space(8); content.addView(metric("Net balance", s.income - s.expense, gold)); space(18)
        section("Top expense categories"); val cats = db.expenseCategories()
        if (cats.isEmpty()) empty("No expense data", "Category analytics will appear after you add expenses.") else cats.forEachIndexed { i, c ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(3), dp(10), dp(3), dp(10)) }
            row.addView(tv("${i+1}. ${c.first}", 13f, text, false), LinearLayout.LayoutParams(0, -2, 1f)); row.addView(tv("SAR ${money.format(c.second)}", 13f, red, true)); content.addView(row)
        }
        space(18); section("Data tools"); content.addView(button("Export & share CSV backup", gold) { shareCsv() }); space(9)
        content.addView(tv("Your accounting database is stored locally on this device. Export backups regularly before changing phones.", 11f, muted, false))
    }

    private fun transactionDialog(type: String) {
        val wrap = dialogForm(); val amount = field("Amount (SAR)", true); val category = field("Category", false); val note = field("Note (optional)", false)
        val accounts = db.accounts(); val spinner = Spinner(this); spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accounts.map { it.name })
        wrap.addView(amount.first); wrap.addView(category.first); wrap.addView(label("Account")); wrap.addView(spinner); wrap.addView(note.first)
        AlertDialog.Builder(this).setTitle(if(type=="INCOME") "Add income" else "Add expense").setView(wrap).setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
            val a = amount.second.text.toString().toDoubleOrNull() ?: 0.0; val cat = category.second.text.toString().ifBlank { if(type=="INCOME") "Income" else "Expense" }
            if (a > 0 && accounts.isNotEmpty()) { db.addTransaction(type, a, cat, accounts[spinner.selectedItemPosition].id, note.second.text.toString()); toast("Saved successfully"); showDashboard() } else toast("Enter a valid amount")
        }.show()
    }

    private fun accountDialog() {
        val wrap = dialogForm(); val name = field("Account name", false); val opening = field("Opening balance (SAR)", true); wrap.addView(name.first); wrap.addView(opening.first)
        AlertDialog.Builder(this).setTitle("New account").setView(wrap).setNegativeButton("Cancel", null).setPositiveButton("Create") { _, _ ->
            if(name.second.text.toString().isNotBlank()) { db.addAccount(name.second.text.toString(), opening.second.text.toString().toDoubleOrNull() ?: 0.0); showDashboard() }
        }.show()
    }

    private fun personDialog() {
        val wrap = dialogForm(); val name = field("Name", false); val phone = field("Phone (optional)", false); wrap.addView(name.first); wrap.addView(phone.first)
        AlertDialog.Builder(this).setTitle("New person ledger").setView(wrap).setNegativeButton("Cancel", null).setPositiveButton("Create") { _, _ ->
            if(name.second.text.toString().isNotBlank()) { db.addPerson(name.second.text.toString(), phone.second.text.toString()); showPeople() }
        }.show()
    }

    private fun personEntryDialog(p: Person) {
        val wrap = dialogForm(); val amount = field("Amount (SAR)", true); val note = field("Note", false); wrap.addView(amount.first); wrap.addView(note.first)
        val modes = arrayOf("I will receive", "I need to pay"); val spin = Spinner(this); spin.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, modes); wrap.addView(label("Entry type")); wrap.addView(spin)
        AlertDialog.Builder(this).setTitle(p.name).setView(wrap).setNegativeButton("Cancel", null).setPositiveButton("Save") { _, _ ->
            val a=amount.second.text.toString().toDoubleOrNull()?:0.0; if(a>0){ db.addPersonEntry(p.id, if(spin.selectedItemPosition==0) a else -a, note.second.text.toString()); showPeople() }
        }.show()
    }

    private fun shareCsv() {
        try {
            val file = File(cacheDir, "hisab-pro-backup-${System.currentTimeMillis()}.csv"); file.writeText(db.csv())
            val uri = FileProvider.getUriForFile(this, "$packageName.files", file)
            val i = Intent(Intent.ACTION_SEND).apply { type="text/csv"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); putExtra(Intent.EXTRA_SUBJECT, "Hisab Pro backup") }
            startActivity(Intent.createChooser(i, "Share backup"))
        } catch(e: Exception) { toast("Could not export backup") }
    }

    private fun stat(name:String, value:Double, color:Int): View { val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; l.addView(tv(name,11f,muted,false)); l.addView(tv("SAR ${money.format(value)}",15f,color,true)); return l }
    private fun metric(name:String, value:Double, color:Int): View { val c=card(panel,16f); val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15),dp(14),dp(15),dp(14));addView(tv(name,11f,muted,false));addView(tv("SAR ${money.format(value)}",19f,color,true).apply{setPadding(0,dp(5),0,0)})};c.addView(l);return c }
    private fun actionCard(title:String, sub:String, color:Int, click:()->Unit): View { val c=card(panel,18f);c.setOnClickListener{click()};val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(15),dp(12),dp(15),dp(12));addView(tv(title,15f,color,true));addView(tv(sub,10f,muted,false))};c.addView(l);return c }
    private fun section(s:String){content.addView(tv(s,15f,text,true).apply{setPadding(0,0,0,dp(10))})}
    private fun empty(a:String,b:String){val c=card(panel,16f);val l=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(20),dp(24),dp(20),dp(24));addView(tv(a,15f,text,true));addView(tv(b,11f,muted,false).apply{gravity=Gravity.CENTER;setPadding(0,dp(5),0,0)})};c.addView(l);content.addView(c)}
    private fun card(color:Int, radius:Float)=MaterialCardView(this).apply{setCardBackgroundColor(color);this.radius=dp(radius.toInt()).toFloat();cardElevation=0f;strokeWidth=1;strokeColor=Color.rgb(37,52,76)}
    private fun button(label:String,color:Int,click:()->Unit)=TextView(this).apply{this.text=label;gravity=Gravity.CENTER;textSize=13f;setTextColor(bg);setTypeface(typeface,1);background=pill(color,14f);setOnClickListener{click()};setPadding(dp(14),dp(13),dp(14),dp(13))}
    private fun tv(s:String,size:Float,color:Int,bold:Boolean)=TextView(this).apply{this.text=s;textSize=size;setTextColor(color);if(bold)setTypeface(typeface,1)}
    private fun pill(color:Int,r:Float)=android.graphics.drawable.GradientDrawable().apply{setColor(color);cornerRadius=dp(r.toInt()).toFloat()}
    private fun space(h:Int){content.addView(View(this),LinearLayout.LayoutParams(1,dp(h)))}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun dialogForm()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(8),dp(20),0)}
    private fun label(s:String)=TextView(this).apply{this.text=s;textSize=12f;setPadding(0,dp(8),0,dp(4))}
    private fun field(hint:String,number:Boolean):Pair<TextInputLayout,TextInputEditText>{val lay=TextInputLayout(this).apply{this.hint=hint;setPadding(0,dp(5),0,dp(5))};val e=TextInputEditText(this);if(number)e.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;lay.addView(e);return lay to e}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
}
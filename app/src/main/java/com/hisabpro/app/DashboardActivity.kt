package com.hisabpro.app

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

class DashboardActivity:AppCompatActivity(){
 private lateinit var db:LedgerDb
 private val money=NumberFormat.getNumberInstance(Locale.US).apply{maximumFractionDigits=2}
 private val navy=Color.rgb(5,38,72);private val navy2=Color.rgb(8,58,94);private val ink=Color.rgb(18,48,82)
 private val muted=Color.rgb(103,124,148);private val page=Color.rgb(244,248,252);private val green=Color.rgb(9,181,128)
 private val green2=Color.rgb(30,204,166);private val red=Color.rgb(255,70,98);private val blue=Color.rgb(47,126,239);private val purple=Color.rgb(116,82,226);private val amber=Color.rgb(255,176,55)
 override fun onCreate(b:Bundle?){super.onCreate(b);db=LedgerDb(this);db.seed();render()}
 private fun render(){
  val s=db.summary();val balance=db.accounts().sumOf{it.balance};val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(page)}
  val scroll=ScrollView(this);val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};scroll.addView(body);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
  val header=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(18),dp(20),dp(34));background=grad(navy,navy2,0)}
  val brand=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
  brand.addView(TextView(this).apply{text="▰";textSize=27f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);background=oval(green,green2)},LinearLayout.LayoutParams(dp(58),dp(58)).apply{marginEnd=dp(13)})
  val names=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(t("Hisab Pro",25,Color.WHITE,true));addView(t("সহজ হিসাব, সুন্দর জীবন",12,Color.rgb(205,225,239),false))}
  brand.addView(names,LinearLayout.LayoutParams(0,-2,1f));brand.addView(t("♢   ●",23,Color.WHITE,false));header.addView(brand);gap(header,24)
  header.addView(t("👋  আসসালামু আলাইকুম",14,Color.rgb(222,236,246),false));header.addView(t("আপনার আজকের হিসাব",21,Color.WHITE,true))
  header.addView(t("▣  "+SimpleDateFormat("EEEE, dd MMMM yyyy",Locale("bn","BD")).format(Date()),13,Color.rgb(200,220,235),false).apply{setPadding(0,dp(7),0,0)});body.addView(header)

  val sheet=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),0,dp(14),dp(24))}
  val bc=card(25);bc.translationY=-dp(18).toFloat();bc.cardElevation=dp(8).toFloat()
  val bb=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(16));background=grad(Color.rgb(13,183,139),Color.rgb(25,201,168),25)}
  val br=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL};br.addView(t("▰",29,Color.WHITE,true))
  val bl=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),0,0,0);addView(t("মোট ব্যালেন্স  ◉",14,Color.WHITE,true));addView(t("SAR "+money.format(balance),30,Color.WHITE,true))}
  br.addView(bl,LinearLayout.LayoutParams(0,-2,1f));br.addView(t("বিস্তারিত  →",12,Color.WHITE,true).apply{setPadding(dp(10),dp(7),dp(10),dp(7));background=round(Color.argb(35,0,0,0),18)});bb.addView(br);gap(bb,14)
  val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;background=round(Color.argb(240,255,255,255),18);setPadding(dp(4),dp(11),dp(4),dp(11))}
  stats.addView(stat("↑","মোট আয়",s.income,green),LinearLayout.LayoutParams(0,-2,1f));stats.addView(stat("↓","মোট খরচ",s.expense,red),LinearLayout.LayoutParams(0,-2,1f));stats.addView(stat("≋","সঞ্চয়",max(0.0,s.income-s.expense),blue),LinearLayout.LayoutParams(0,-2,1f));bb.addView(stats);bc.addView(bb);sheet.addView(bc)

  val qc=card(22);val q=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(dp(6),dp(14),dp(6),dp(14))}
  q.addView(action("+","আয় যোগ\nকরুন",green),LinearLayout.LayoutParams(0,-2,1f));q.addView(action("−","খরচ যোগ\nকরুন",red),LinearLayout.LayoutParams(0,-2,1f));q.addView(action("▤","লেনদেন\nতালিকা",blue),LinearLayout.LayoutParams(0,-2,1f));q.addView(action("◔","পাওনা-দেনা",purple),LinearLayout.LayoutParams(0,-2,1f));q.addView(action("▥","রিপোর্ট",amber),LinearLayout.LayoutParams(0,-2,1f));qc.addView(q);sheet.addView(qc);gap(sheet,16)

  section(sheet,"▦  খরচের বিভাগ","সব দেখুন  →");val cat=card(22);val cw=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(7),dp(9),dp(7),dp(9))}
  val data=listOf(arrayOf("🍴","খাবার"),arrayOf("⌂","বাড়িভাড়া"),arrayOf("ϟ","বিদ্যুৎ"),arrayOf("▣","মোবাইল"),arrayOf("▣","কেনাকাটা"),arrayOf("▰","যাতায়াত"),arrayOf("♥","স্বাস্থ্য"),arrayOf("•••","অন্যান্য"));val cols=listOf(amber,purple,green,blue,red,blue,red,blue)
  for(r in 0..1){val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};for(i in 0..3){val n=r*4+i;row.addView(category(data[n][0],data[n][1],cols[n]),LinearLayout.LayoutParams(0,dp(100),1f).apply{setMargins(dp(3),dp(3),dp(3),dp(3))})};cw.addView(row)};cat.addView(cw);sheet.addView(cat);gap(sheet,16)

  section(sheet,"◴  সাম্প্রতিক লেনদেন","সব দেখুন  →");val rc=card(22);val rw=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(7),dp(6),dp(7),dp(6))};val recent=db.transactions(4)
  if(recent.isEmpty())rw.addView(t("এখনও কোনো লেনদেন নেই",12,muted,false).apply{gravity=Gravity.CENTER;setPadding(0,dp(22),0,dp(22))}) else recent.forEach{x->val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(6),dp(9),dp(6),dp(9))};val c=if(x.type=="INCOME")green else red;row.addView(t(if(x.type=="INCOME")"↑" else "↓",19,c,true).apply{gravity=Gravity.CENTER;background=round(if(x.type=="INCOME")Color.rgb(231,249,243) else Color.rgb(255,237,240),14)},LinearLayout.LayoutParams(dp(44),dp(44)).apply{marginEnd=dp(10)});val mid=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(t(x.category,13,ink,true));addView(t(x.account+" • "+x.date,10,muted,false))};row.addView(mid,LinearLayout.LayoutParams(0,-2,1f));row.addView(t((if(x.type=="INCOME")"+" else "−")+" SAR "+money.format(x.amount),12,c,true));rw.addView(row)};rc.addView(rw);sheet.addView(rc);body.addView(sheet)

  val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(dp(5),dp(6),dp(5),dp(7));setBackgroundColor(Color.WHITE);elevation=dp(10).toFloat()}
  nav.addView(navItem("⌂","হোম",green),LinearLayout.LayoutParams(0,-1,1f));nav.addView(navItem("⇄","লেনদেন",muted),LinearLayout.LayoutParams(0,-1,1f));nav.addView(TextView(this).apply{text="+";textSize=31f;gravity=Gravity.CENTER;setTextColor(Color.WHITE);background=oval(green,green2);setOnClickListener{openFull()}},LinearLayout.LayoutParams(0,dp(58),1f));nav.addView(navItem("▥","রিপোর্ট",muted),LinearLayout.LayoutParams(0,-1,1f));nav.addView(navItem("•••","আরও",muted),LinearLayout.LayoutParams(0,-1,1f));root.addView(nav,LinearLayout.LayoutParams(-1,dp(76)));setContentView(root)
 }
 private fun openFull(){startActivity(Intent(this,MainActivity::class.java))}
 private fun action(icon:String,label:String,color:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{openFull()};addView(t(icon,21,Color.WHITE,true).apply{gravity=Gravity.CENTER;background=round(color,15)},LinearLayout.LayoutParams(dp(47),dp(47)));addView(t(label,10,ink,true).apply{gravity=Gravity.CENTER;setPadding(0,dp(6),0,0)})}
 private fun stat(icon:String,label:String,v:Double,c:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;addView(t(icon,17,c,true).apply{gravity=Gravity.CENTER});addView(t(label,10,muted,false).apply{gravity=Gravity.CENTER});addView(t("SAR "+money.format(v),11,ink,true).apply{gravity=Gravity.CENTER})}
 private fun category(icon:String,label:String,c:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;background=round(Color.rgb(249,251,254),16);addView(t(icon,19,c,true).apply{gravity=Gravity.CENTER});addView(t(label,10,ink,true).apply{gravity=Gravity.CENTER;setPadding(0,dp(5),0,0)})}
 private fun navItem(icon:String,label:String,c:Int)=TextView(this).apply{text=icon+"\n"+label;textSize=11f;gravity=Gravity.CENTER;setTextColor(c);setOnClickListener{openFull()}}
 private fun section(p:LinearLayout,a:String,b:String){val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(4),0,dp(4),dp(9))};r.addView(t(a,15,ink,true),LinearLayout.LayoutParams(0,-2,1f));r.addView(t(b,11,Color.rgb(39,139,170),true));p.addView(r)}
 private fun card(r:Int)=MaterialCardView(this).apply{setCardBackgroundColor(Color.WHITE);radius=dp(r).toFloat();strokeWidth=0;cardElevation=dp(2).toFloat()}
 private fun t(s:String,z:Int,c:Int,b:Boolean)=TextView(this).apply{text=s;textSize=z.toFloat();setTextColor(c);if(b)setTypeface(typeface,Typeface.BOLD)}
 private fun round(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
 private fun grad(a:Int,b:Int,r:Int)=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(a,b)).apply{cornerRadius=dp(r).toFloat()}
 private fun oval(a:Int,b:Int)=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(a,b)).apply{shape=GradientDrawable.OVAL}
 private fun gap(p:LinearLayout,h:Int){p.addView(View(this),LinearLayout.LayoutParams(1,dp(h)))};private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
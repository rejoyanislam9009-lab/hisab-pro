package com.hisabpro.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

data class Account(val id:Long,val name:String,val type:String,val balance:Double)
data class Txn(val id:Long,val type:String,val amount:Double,val category:String,val account:String,val note:String,val date:String)
data class Person(val id:Long,val name:String,val phone:String)
data class Summary(val income:Double,val expense:Double,val receivable:Double,val payable:Double)

class LedgerDb(ctx: Context): SQLiteOpenHelper(ctx,"hisab_pro.db",null,1){
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE accounts(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,type TEXT NOT NULL DEFAULT 'Cash / Bank',opening REAL NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE transactions(id INTEGER PRIMARY KEY AUTOINCREMENT,type TEXT NOT NULL,amount REAL NOT NULL,category TEXT NOT NULL,account_id INTEGER NOT NULL,note TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE people(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT NOT NULL DEFAULT '')")
        db.execSQL("CREATE TABLE person_entries(id INTEGER PRIMARY KEY AUTOINCREMENT,person_id INTEGER NOT NULL,amount REAL NOT NULL,note TEXT NOT NULL DEFAULT '',created_at INTEGER NOT NULL)")
    }
    override fun onUpgrade(db:SQLiteDatabase,oldVersion:Int,newVersion:Int){}
    fun seed(){ if(accounts().isEmpty()){ addAccount("Cash Wallet",0.0); addAccount("Bank Account",0.0) } }
    fun addAccount(name:String,opening:Double){writableDatabase.insert("accounts",null,ContentValues().apply{put("name",name);put("type","Cash / Bank");put("opening",opening)})}
    fun accounts():List<Account>{
        val out= mutableListOf<Account>()
        val sql="""SELECT a.id,a.name,a.type,a.opening + COALESCE(SUM(CASE WHEN t.type='INCOME' THEN t.amount ELSE -t.amount END),0) balance FROM accounts a LEFT JOIN transactions t ON a.id=t.account_id GROUP BY a.id ORDER BY a.id"""
        readableDatabase.rawQuery(sql,null).use{c->while(c.moveToNext())out+=Account(c.getLong(0),c.getString(1),c.getString(2),c.getDouble(3))}
        return out
    }
    fun addTransaction(type:String,amount:Double,category:String,accountId:Long,note:String){
        writableDatabase.insert("transactions",null,ContentValues().apply{put("type",type);put("amount",amount);put("category",category);put("account_id",accountId);put("note",note);put("created_at",System.currentTimeMillis())})
    }
    fun transactions(limit:Int):List<Txn>{
        val out= mutableListOf<Txn>(); val f=SimpleDateFormat("dd MMM yyyy",Locale.US)
        readableDatabase.rawQuery("SELECT t.id,t.type,t.amount,t.category,a.name,t.note,t.created_at FROM transactions t JOIN accounts a ON a.id=t.account_id ORDER BY t.created_at DESC LIMIT ?",arrayOf(limit.toString())).use{c->
            while(c.moveToNext())out+=Txn(c.getLong(0),c.getString(1),c.getDouble(2),c.getString(3),c.getString(4),c.getString(5),f.format(Date(c.getLong(6))))
        }
        return out
    }
    fun addPerson(name:String,phone:String){writableDatabase.insert("people",null,ContentValues().apply{put("name",name);put("phone",phone)})}
    fun people():List<Person>{val out= mutableListOf<Person>();readableDatabase.rawQuery("SELECT id,name,phone FROM people ORDER BY name",null).use{c->while(c.moveToNext())out+=Person(c.getLong(0),c.getString(1),c.getString(2))};return out}
    fun addPersonEntry(personId:Long,amount:Double,note:String){writableDatabase.insert("person_entries",null,ContentValues().apply{put("person_id",personId);put("amount",amount);put("note",note);put("created_at",System.currentTimeMillis())})}
    fun personBalance(id:Long):Double=readableDatabase.rawQuery("SELECT COALESCE(SUM(amount),0) FROM person_entries WHERE person_id=?",arrayOf(id.toString())).use{c->c.moveToFirst();c.getDouble(0)}
    fun summary():Summary{
        val inc=scalar("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='INCOME'")
        val exp=scalar("SELECT COALESCE(SUM(amount),0) FROM transactions WHERE type='EXPENSE'")
        val rec=scalar("SELECT COALESCE(SUM(CASE WHEN amount>0 THEN amount ELSE 0 END),0) FROM person_entries")
        val pay=-scalar("SELECT COALESCE(SUM(CASE WHEN amount<0 THEN amount ELSE 0 END),0) FROM person_entries")
        return Summary(inc,exp,rec,pay)
    }
    fun expenseCategories():List<Pair<String,Double>>{
        val out= mutableListOf<Pair<String,Double>>()
        readableDatabase.rawQuery("SELECT category,SUM(amount) total FROM transactions WHERE type='EXPENSE' GROUP BY category ORDER BY total DESC LIMIT 8",null).use{c->while(c.moveToNext())out+=c.getString(0) to c.getDouble(1)}
        return out
    }
    private fun scalar(sql:String):Double=readableDatabase.rawQuery(sql,null).use{c->c.moveToFirst();c.getDouble(0)}
    fun csv():String{
        val b=StringBuilder("Hisab Pro Backup\nType,Amount SAR,Category,Account,Note,Date\n")
        transactions(100000).forEach{t->b.append(listOf(t.type,t.amount.toString(),q(t.category),q(t.account),q(t.note),t.date).joinToString(",")).append("\n")}
        b.append("\nPeople Ledger\nName,Phone,Balance SAR\n")
        people().forEach{p->b.append("${q(p.name)},${q(p.phone)},${personBalance(p.id)}\n")}
        return b.toString()
    }
    private fun q(s:String)="\"${s.replace("\"","\"\"")}\""
}
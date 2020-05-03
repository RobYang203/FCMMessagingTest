package com.test.testfcmmessaging

import android.app.usage.UsageStats

class RecentUseComparator:Comparator<UsageStats> {
    override fun compare(lhs: UsageStats?, rhs: UsageStats?): Int {
        return if(lhs!!.lastTimeUsed > rhs!!.lastTimeUsed){
            -1
        }else if(lhs?.lastTimeUsed == rhs?.lastTimeUsed){
            0
        }else{
            1
        }
    }
}
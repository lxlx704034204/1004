package com.hongbaogou.utils;

import android.content.Context;
import android.text.TextUtils;

/**
 * Created by Administrator on 2015/12/22.
 */
public class MobileUtils {
    /**
     * 验证手机格式
     */
    public static boolean isMobileNO(String mobiles, Context context) {
    /*
    移动：134、135、136、137、138、139、150、151、157(TD)、158、159、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、180、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或7或8，其他位置的可以为0-9
    */

        //"[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        String telRegex = "[1][3578]\\d{9}";

        if (TextUtils.isEmpty(mobiles)) {
            return false;
        } else if (mobiles.length() < 11 || mobiles.length() > 11) {
            ToastUtil.showToast(context, "您输入了 " + mobiles.length() + " 位数字");
            return false;
        } else {
            return mobiles.matches(telRegex);
        }
    }
}

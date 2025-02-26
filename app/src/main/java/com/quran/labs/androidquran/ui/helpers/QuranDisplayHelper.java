package com.quran.labs.androidquran.ui.helpers;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import androidx.annotation.NonNull;
import android.view.Display;
import android.widget.Toast;

import com.quran.data.core.QuranInfo;
import com.quran.labs.androidquran.R;
import com.quran.labs.androidquran.common.Response;
import com.quran.labs.androidquran.ui.util.ToastCompat;
import com.quran.labs.androidquran.util.QuranFileUtils;
import com.quran.labs.androidquran.util.QuranUtils;

import okhttp3.OkHttpClient;
import timber.log.Timber;

public class QuranDisplayHelper {

  @NonNull
  static Response getQuranPage(OkHttpClient okHttpClient,
                               Context context,
                               String widthParam,
                               int page,
                               QuranFileUtils quranFileUtils) {
    Response response;
    String filename = QuranFileUtils.getPageFileName(page);
    response = quranFileUtils.getImageFromSD(context, widthParam, filename);
    if (!response.isSuccessful()) {
      // let's only try if an sdcard is found... otherwise, let's tell
      // the user to mount their sdcard and try again.
      if (response.getErrorCode() != Response.ERROR_SD_CARD_NOT_FOUND) {
        Timber.d("failed to get %d with name %s from sd...", page, filename);

        if (!QuranUtils.haveInternet(context)) {
          final Response noNetworkResponse = new Response(Response.ERROR_NO_INTERNET);
          noNetworkResponse.setPageData(page);
          response = noNetworkResponse;
        } else {
          response = quranFileUtils.getImageFromWeb(okHttpClient, context, widthParam, filename);
        }
      }
    }
    return response;
  }

  public static long displayMarkerPopup(Context context,
                                        QuranInfo quranInfo,
                                        int page,
                                        long lastPopupTime) {
    if (System.currentTimeMillis() - lastPopupTime < 3000) {
      return lastPopupTime;
    }
    int rub3 = quranInfo.getRub3FromPage(page);
    if (rub3 == -1) {
      return lastPopupTime;
    }
    int hizb = (rub3 / 4) + 1;
    StringBuilder sb = new StringBuilder();

    if (rub3 % 8 == 0) {
      sb.append(context.getString(R.string.quran_juz2)).append(' ')
          .append(QuranUtils.getLocalizedNumber(context,
              (hizb / 2) + 1));
    } else {
      int remainder = rub3 % 4;
      if (remainder == 1) {
        sb.append(context.getString(R.string.quran_rob3)).append(' ');
      } else if (remainder == 2) {
        sb.append(context.getString(R.string.quran_nos)).append(' ');
      } else if (remainder == 3) {
        sb.append(context.getString(R.string.quran_talt_arb3)).append(' ');
      }
      sb.append(context.getString(R.string.quran_hizb)).append(' ')
          .append(QuranUtils.getLocalizedNumber(context, hizb));
    }
//Me{: Make each Joz' 4 hizbs instead of two 4-part hizbs
    sb = new StringBuilder();
    if (rub3 % 2 == 1) return lastPopupTime; hizb=rub3/2+1;
    if (rub3 % 8 == 0) {
      sb.append(context.getString(R.string.quran_juz2)).append(' ')
          .append(QuranUtils.getLocalizedNumber(context,
              (hizb / 4) + 1));
    }else{ sb.append(context.getString(R.string.quran_hizb)).append(' ')
        .append(QuranUtils.getLocalizedNumber(context, hizb));
    }
//Me}
    String result = sb.toString();
    ToastCompat.makeText(context, result, Toast.LENGTH_SHORT).show();
    return System.currentTimeMillis();
  }

  // same logic used in displayMarkerPopup method
  public static String displayRub3(Context context, QuranInfo quranInfo, int page){
    int rub3 = quranInfo.getRub3FromPage(page);
    if (rub3 == -1) {
      return "";
    }
    if (rub3 % 2 == 1) return ""; else return new StringBuilder()
        .append(context.getString(R.string.comma_with_spaces))
        .append(context.getString(R.string.quran_hizb))
        .append(' ')
        .append(QuranUtils.getLocalizedNumber(context, rub3 / 2 +
            1)).toString();
  /*Me Changed this with the preceding statement
    int hizb = (rub3 / 4) + 1;
    StringBuilder sb = new StringBuilder();
    sb.append(context.getString(R.string.comma_with_spaces));
    int remainder = rub3 % 4;
    if (remainder == 1) {
      sb.append(context.getString(R.string.quran_rob3)).append(' ');
    } else if (remainder == 2) {
      sb.append(context.getString(R.string.quran_nos)).append(' ');
    } else if (remainder == 3) {
      sb.append(context.getString(R.string.quran_talt_arb3)).append(' ');
    }
    sb.append(context.getString(R.string.quran_hizb)).append(' ')
            .append(QuranUtils.getLocalizedNumber(context, hizb));

    return sb.toString();
  */
  }

  public static PaintDrawable getPaintDrawable(int startX, int endX) {
    PaintDrawable drawable = new PaintDrawable();
    drawable.setShape(new RectShape());
    drawable.setShaderFactory(getShaderFactory(startX, endX));
    return drawable;
  }

  private static ShapeDrawable.ShaderFactory getShaderFactory(final int startX, final int endX) {
    return new ShapeDrawable.ShaderFactory() {

      @Override
      public Shader resize(int width, int height) {
        return new LinearGradient(startX, 0, endX, 0,
            new int[]{0xFFDCDAD5, 0xFFFDFDF4,
                0xFFFFFFFF, 0xFFFDFBEF},
            new float[]{0, 0.18f, 0.48f, 1},
            Shader.TileMode.REPEAT);
      }
    };
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  public static int getWidthKitKat(Display display) {
    Point point = new Point();
    display.getRealSize(point);
    return point.x;
  }
}

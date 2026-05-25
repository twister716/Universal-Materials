package io.github.twister716.universalmaterials.client.texture;

import java.awt.image.BufferedImage;

/**
 * グレースケールテクスチャを2色グラデーションで着色するユーティリティクラス。
 *
 * テクスチャ合成の流れ:
 *   1. グレースケール画像のRチャンネル（輝度）を t として取得する（0.0〜1.0）
 *   2. t=1.0 のとき primaryColor、t=0.0 のとき secondaryColor になるよう線形補間する
 *   3. アルファ値はそのまま保持する
 *
 * 鉱石テクスチャの合成には compositeOver() で石テクスチャの上にオーバーレイを重ねる。
 */
public class TextureColorizer {

    /**
     * グレースケール画像を2色グラデーションで着色して返す。
     *
     * @param src             グレースケールのBufferedImage
     * @param primaryColor    輝度が高い（明るい）ピクセルの色（RGB 0xRRGGBB）
     * @param secondaryColor  輝度が低い（暗い）ピクセルの色（RGB 0xRRGGBB）
     * @param transparentBlack 輝度が0.1未満のピクセルを透明にするか
     */
    public static BufferedImage colorize(BufferedImage src, int primaryColor, int secondaryColor,
                                         boolean transparentBlack) {
        int width  = src.getWidth();
        int height = src.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int pR = (primaryColor >> 16) & 0xFF;
        int pG = (primaryColor >>  8) & 0xFF;
        int pB =  primaryColor        & 0xFF;

        int sR = (secondaryColor >> 16) & 0xFF;
        int sG = (secondaryColor >>  8) & 0xFF;
        int sB =  secondaryColor        & 0xFF;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb  = src.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;

                if (alpha == 0) {
                    result.setRGB(x, y, 0x00000000);
                    continue;
                }

                // Rチャンネルを輝度として使う（グレースケール画像なのでR=G=B）
                float t = ((argb >> 16) & 0xFF) / 255.0f;

                if (transparentBlack && t < 0.1f) {
                    result.setRGB(x, y, 0x00000000);
                    continue;
                }

                int r = Math.round(sR + (pR - sR) * t);
                int g = Math.round(sG + (pG - sG) * t);
                int b = Math.round(sB + (pB - sB) * t);

                r = Math.clamp(r, 0, 255);
                g = Math.clamp(g, 0, 255);
                b = Math.clamp(b, 0, 255);

                result.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    /** transparentBlack=false版（通常アイテムテクスチャ用） */
    public static BufferedImage colorize(BufferedImage src, int primaryColor, int secondaryColor) {
        return colorize(src, primaryColor, secondaryColor, false);
    }

    /**
     * カラー画像を輝度ベースのグレースケールに変換する。
     * 溶岩・水などのカラーテクスチャを着色前に変換するために使う。
     * ITU-R BT.601の重み付き平均で輝度を計算する。
     */
    public static BufferedImage toGrayscale(BufferedImage src) {
        int width  = src.getWidth();
        int height = src.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb  = src.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;

                if (alpha == 0) {
                    result.setRGB(x, y, 0x00000000);
                    continue;
                }

                int r = (argb >> 16) & 0xFF;
                int g = (argb >>  8) & 0xFF;
                int b =  argb        & 0xFF;

                int luma = (int)(r * 0.299 + g * 0.587 + b * 0.114);
                luma = Math.clamp(luma, 0, 255);

                result.setRGB(x, y, (alpha << 24) | (luma << 16) | (luma << 8) | luma);
            }
        }
        return result;
    }

    /**
     * base画像の上にoverlay画像をアルファブレンドで重ねる。
     * 鉱石テクスチャの合成（石テクスチャ + 着色済みオーバーレイ）に使う。
     */
    public static BufferedImage compositeOver(BufferedImage base, BufferedImage overlay) {
        int width  = base.getWidth();
        int height = base.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int baseArgb     = base.getRGB(x, y);
                int overlayArgb  = overlay.getRGB(x, y);
                int overlayAlpha = (overlayArgb >> 24) & 0xFF;

                if (overlayAlpha == 0) {
                    result.setRGB(x, y, baseArgb);
                } else if (overlayAlpha == 255) {
                    result.setRGB(x, y, overlayArgb);
                } else {
                    float a = overlayAlpha / 255.0f;
                    int bR = (baseArgb >> 16) & 0xFF;
                    int bG = (baseArgb >>  8) & 0xFF;
                    int bB =  baseArgb        & 0xFF;
                    int oR = (overlayArgb >> 16) & 0xFF;
                    int oG = (overlayArgb >>  8) & 0xFF;
                    int oB =  overlayArgb        & 0xFF;

                    int r = Math.round(oR * a + bR * (1 - a));
                    int g = Math.round(oG * a + bG * (1 - a));
                    int b = Math.round(oB * a + bB * (1 - a));

                    result.setRGB(x, y, (255 << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
        return result;
    }

    /**
     * 輝度が0.1未満のピクセルを透明にする（それ以外はそのまま）。
     * noColorize()指定の鉱石オーバーレイ（専用テクスチャの黒背景を透過させる）に使う。
     */
    public static BufferedImage makeTransparentBlackOnly(BufferedImage src) {
        int width  = src.getWidth();
        int height = src.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb  = src.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha == 0) {
                    result.setRGB(x, y, 0x00000000);
                    continue;
                }
                float t = ((argb >> 16) & 0xFF) / 255.0f;
                result.setRGB(x, y, t < 0.1f ? 0x00000000 : argb);
            }
        }
        return result;
    }
}
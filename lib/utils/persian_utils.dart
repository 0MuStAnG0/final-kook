// Persian Formatting Utilities for Kook Flutter / PWA

class PersianUtils {
  static const List<String> persianDigits = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];

  static String toPersianDigits(dynamic input) {
    if (input == null) return '';
    String str = input.toString();
    for (int i = 0; i < 10; i++) {
      str = str.replaceAll(i.toString(), persianDigits[i]);
    }
    return str;
  }

  static String formatToman(dynamic amount) {
    if (amount == null) return '۰ تومان';
    int val = 0;
    if (amount is int) {
      val = amount;
    } else if (amount is double) {
      val = amount.toInt();
    } else if (amount is String) {
      val = int.tryParse(amount.replaceAll(RegExp(r'[^0-9]'), '')) ?? 0;
    }
    
    // Group by 3 digits
    String formatted = val.toString().replaceAllMapped(
      RegExp(r'(\d{1,3})(?=(\d{3})+(?!\d))'),
      (Match m) => '${m[1]}،',
    );
    return '${toPersianDigits(formatted)} تومان';
  }

  static String formatCompactNumber(int number) {
    if (number >= 1000000) {
      double m = number / 1000000.0;
      return '${toPersianDigits(m.toStringAsFixed(1))}M';
    } else if (number >= 1000) {
      double k = number / 1000.0;
      return '${toPersianDigits(k.toStringAsFixed(1))}K';
    }
    return toPersianDigits(number);
  }
}

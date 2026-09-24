import 'package:flutter/material.dart';

class AppTheme {
  // Brand Colors
  static const Color primary = Color(0xFF4F46E5); // Indigo Primary
  static const Color primaryDark = Color(0xFF3730A3);
  static const Color secondary = Color(0xFFF59E0B); // Amber Accent
  static const Color accentPurple = Color(0xFF9333EA);
  static const Color success = Color(0xFF10B981);
  static const Color error = Color(0xFFEF4444);

  // Dark Theme Palette
  static const Color darkBackground = Color(0xFF0B0F19);
  static const Color darkSurface = Color(0xFF131B2E);
  static const Color darkCard = Color(0xFF1E293B);
  static const Color darkBorder = Color(0xFF334155);
  static const Color textPrimaryDark = Color(0xFFF8FAFC);
  static const Color textSecondaryDark = Color(0xFF94A3B8);

  // Light Theme Palette
  static const Color lightBackground = Color(0xFFF8FAFC);
  static const Color lightSurface = Color(0xFFFFFFFF);
  static const Color lightCard = Color(0xFFFFFFFF);
  static const Color lightBorder = Color(0xFFE2E8F0);
  static const Color textPrimaryLight = Color(0xFF0F172A);
  static const Color textSecondaryLight = Color(0xFF64748B);

  static ThemeData darkTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: darkBackground,
    fontFamily: 'Vazirmatn',
    colorScheme: const ColorScheme.dark(
      primary: primary,
      secondary: secondary,
      surface: darkSurface,
      background: darkBackground,
      error: error,
      onPrimary: Colors.white,
      onSecondary: Colors.black,
      onSurface: textPrimaryDark,
      onBackground: textPrimaryDark,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: darkSurface,
      elevation: 0,
      centerTitle: true,
      titleTextStyle: TextStyle(
        fontFamily: 'Vazirmatn',
        fontSize: 18,
        fontWeight: FontWeight.bold,
        color: textPrimaryDark,
      ),
    ),
    cardTheme: CardTheme(
      color: darkCard,
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: const BorderSide(color: darkBorder, width: 1),
      ),
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: darkSurface,
      selectedItemColor: primary,
      unselectedItemColor: textSecondaryDark,
      type: BottomNavigationBarType.fixed,
      elevation: 8,
    ),
  );

  static ThemeData lightTheme = ThemeData(
    useMaterial3: true,
    brightness: Brightness.light,
    scaffoldBackgroundColor: lightBackground,
    fontFamily: 'Vazirmatn',
    colorScheme: const ColorScheme.light(
      primary: primary,
      secondary: secondary,
      surface: lightSurface,
      background: lightBackground,
      error: error,
      onPrimary: Colors.white,
      onSecondary: Colors.black,
      onSurface: textPrimaryLight,
      onBackground: textPrimaryLight,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: lightSurface,
      elevation: 0,
      centerTitle: true,
      titleTextStyle: TextStyle(
        fontFamily: 'Vazirmatn',
        fontSize: 18,
        fontWeight: FontWeight.bold,
        color: textPrimaryLight,
      ),
    ),
    cardTheme: CardTheme(
      color: lightCard,
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: const BorderSide(color: lightBorder, width: 1),
      ),
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: lightSurface,
      selectedItemColor: primary,
      unselectedItemColor: textSecondaryLight,
      type: BottomNavigationBarType.fixed,
      elevation: 8,
    ),
  );
}

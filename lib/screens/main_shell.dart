import 'package:flutter/material.dart';
import 'feed_screen.dart';
import 'discover_screen.dart';
import 'add_screen.dart';
import 'map_screen.dart';
import 'profile_screen.dart';

class MainShell extends StatefulWidget {
  const MainShell({Key? key}) : super(key: key);

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _currentIndex = 0;

  final List<Widget> _screens = const [
    FeedScreen(),
    DiscoverScreen(),
    SizedBox(), // Placeholder for Add modal
    MapScreen(),
    ProfileScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(
        index: _currentIndex == 2 ? 0 : _currentIndex,
        children: _screens,
      ),
      bottomNavigationBar: BottomNavigationBar(
        currentIndex: _currentIndex,
        onTap: (index) {
          if (index == 2) {
            Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const AddScreen()),
            );
          } else {
            setState(() => _currentIndex = index);
          }
        },
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.play_circle_outline),
            activeIcon: Icon(Icons.play_circle_fill),
            label: 'فید ویدیو',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.search),
            activeIcon: Icon(Icons.search, weight: 800),
            label: 'بازار و کاوش',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.add_box_outlined, size: 28),
            activeIcon: Icon(Icons.add_box, size: 28),
            label: 'ثبت محصول',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.map_outlined),
            activeIcon: Icon(Icons.map),
            label: 'نقشه فروشگاه‌ها',
          ),
          BottomNavigationBarItem(
            icon: Icon(Icons.person_outline),
            activeIcon: Icon(Icons.person),
            label: 'پروفایل',
          ),
        ],
      ),
    );
  }
}

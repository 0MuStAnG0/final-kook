import 'package:flutter/material.dart';

class ResponsiveWrapper extends StatelessWidget {
  final Widget child;

  const ResponsiveWrapper({Key? key, required this.child}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return LayoutBuilder(
      builder: (context, constraints) {
        // If screen is wider than 650dp (Desktop Web / Tablet), wrap in modern mobile frame
        if (constraints.maxWidth > 650) {
          return Scaffold(
            backgroundColor: const Color(0xFF070A11),
            body: Center(
              child: Container(
                width: 440,
                height: constraints.maxHeight > 900 ? 900 : constraints.maxHeight,
                margin: const EdgeInsets.symmetric(vertical: 16),
                decoration: BoxDecoration(
                  color: Theme.of(context).scaffoldBackgroundColor,
                  borderRadius: BorderRadius.circular(28),
                  border: Border.all(color: const Color(0xFF334155), width: 2),
                  boxShadow: [
                    BoxShadow(
                      color: const Color(0xFF4F46E5).withOpacity(0.15),
                      blurRadius: 35,
                      offset: const Offset(0, 10),
                    ),
                  ],
                ),
                clipBehavior: Clip.antiAlias,
                child: child,
              ),
            ),
          );
        }

        // On mobile and standard PWA viewport, render full screen edge-to-edge
        return child;
      },
    );
  }
}

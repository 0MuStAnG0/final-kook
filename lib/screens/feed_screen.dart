import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../state/app_state.dart';
import '../utils/persian_utils.dart';
import '../widgets/live_shopping_sheet.dart';

class FeedScreen extends StatefulWidget {
  const FeedScreen({Key? key}) : super(key: key);

  @override
  State<FeedScreen> createState() => _FeedScreenState();
}

class _FeedScreenState extends State<FeedScreen> {
  final PageController _pageController = PageController();

  @override
  void dispose() {
    _pageController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final state = Provider.of<AppStateManager>(context);
    final posts = state.posts;

    if (posts.isEmpty) {
      return const Center(child: Text('پستی موجود نیست'));
    }

    return Scaffold(
      backgroundColor: Colors.black,
      body: PageView.builder(
        controller: _pageController,
        scrollDirection: Axis.vertical,
        itemCount: posts.length,
        itemBuilder: (context, index) {
          final post = posts[index];
          return _buildPostItem(context, post, state);
        },
      ),
    );
  }

  Widget _buildPostItem(BuildContext context, Post post, AppStateManager state) {
    return Stack(
      fit: StackFit.expand,
      children: [
        // Background media (Image/Video poster)
        Image.network(
          post.imageUrl,
          fit: BoxFit.cover,
          errorBuilder: (_, __, ___) => Container(
            color: const Color(0xFF1E293B),
            child: const Center(
              child: Icon(Icons.broken_image, size: 60, color: Colors.grey),
            ),
          ),
        ),

        // Gradient overlay
        Container(
          decoration: const BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                Colors.black38,
                Colors.transparent,
                Colors.black87,
              ],
              stops: [0.0, 0.5, 1.0],
            ),
          ),
        ),

        // Side action buttons (Like, Comment, Share, Save)
        Positioned(
          left: 16,
          bottom: 40,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Like
              IconButton(
                icon: Icon(
                  post.isLiked ? Icons.favorite : Icons.favorite_border,
                  color: post.isLiked ? Colors.redAccent : Colors.white,
                  size: 32,
                ),
                onPressed: () => state.toggleLike(post.id),
              ),
              Text(
                PersianUtils.formatCompactNumber(post.likesCount),
                style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),

              // Comments
              IconButton(
                icon: const Icon(Icons.mode_comment_outlined, color: Colors.white, size: 30),
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('بخش نظرات به زودی فعال می‌شود')),
                  );
                },
              ),
              Text(
                PersianUtils.formatCompactNumber(post.commentsCount),
                style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),

              // Bookmark / Save
              IconButton(
                icon: Icon(
                  post.isSaved ? Icons.bookmark : Icons.bookmark_border,
                  color: post.isSaved ? Colors.amber : Colors.white,
                  size: 30,
                ),
                onPressed: () => state.toggleSave(post.id),
              ),
              Text(
                PersianUtils.formatCompactNumber(post.savesCount),
                style: const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),

              // Share
              IconButton(
                icon: const Icon(Icons.share, color: Colors.white, size: 28),
                onPressed: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('لینک ویدیو کپی شد')),
                  );
                },
              ),
            ],
          ),
        ),

        // Bottom Content (Seller profile, description, sound & Live Shopping CTA)
        Positioned(
          right: 16,
          left: 80,
          bottom: 30,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisSize: MainAxisSize.min,
            children: [
              // Live Shopping Pill if post has product
              if (post.product != null) ...[
                InkWell(
                  onTap: () => LiveShoppingSheet.show(context, post.product!, post.author),
                  child: Container(
                    margin: const EdgeInsets.only(bottom: 12),
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
                    decoration: BoxDecoration(
                      gradient: const LinearGradient(
                        colors: [Color(0xFF4F46E5), Color(0xFF9333EA)],
                      ),
                      borderRadius: BorderRadius.circular(20),
                      boxShadow: [
                        BoxShadow(
                          color: const Color(0xFF4F46E5).withOpacity(0.5),
                          blurRadius: 10,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.shopping_bag, color: Colors.white, size: 18),
                        const SizedBox(width: 8),
                        Text(
                          '${post.product!.title} • ${PersianUtils.formatToman(post.product!.price)}',
                          style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 13),
                        ),
                        const SizedBox(width: 6),
                        const Icon(Icons.arrow_back_ios, color: Colors.white, size: 12),
                      ],
                    ),
                  ),
                ),
              ],

              // Author info
              Row(
                children: [
                  CircleAvatar(
                    radius: 18,
                    backgroundImage: NetworkImage(post.author.avatarUrl),
                  ),
                  const SizedBox(width: 10),
                  Text(
                    post.author.name,
                    style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 15),
                  ),
                  if (post.author.isVerified) ...[
                    const SizedBox(width: 4),
                    const Icon(Icons.verified, color: Colors.blueAccent, size: 16),
                  ],
                ],
              ),
              const SizedBox(height: 8),

              // Description
              Text(
                post.description,
                style: const TextStyle(color: Colors.white70, fontSize: 13, height: 1.4),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
              const SizedBox(height: 10),

              // Sound track
              Row(
                children: [
                  const Icon(Icons.music_note, color: Colors.white60, size: 16),
                  const SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      post.audioTrack,
                      style: const TextStyle(color: Colors.white70, fontSize: 12),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ],
    );
  }
}

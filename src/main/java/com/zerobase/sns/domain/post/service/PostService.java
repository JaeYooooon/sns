package com.zerobase.sns.domain.post.service;

import static com.zerobase.sns.global.exception.ErrorCode.NOT_FOUND_POST;
import static com.zerobase.sns.global.exception.ErrorCode.NOT_FOUND_USER;
import static com.zerobase.sns.global.exception.ErrorCode.UNAUTHORIZED_ACCESS;

import com.zerobase.sns.domain.alarm.entity.Alarm;
import com.zerobase.sns.domain.alarm.repository.AlarmRepository;
import com.zerobase.sns.domain.post.dto.PostCreateDTO;
import com.zerobase.sns.domain.post.dto.PostUpdateDTO;
import com.zerobase.sns.domain.post.entity.Post;
import com.zerobase.sns.domain.post.repository.PostRepository;
import com.zerobase.sns.domain.tag.entity.Tag;
import com.zerobase.sns.domain.tag.repository.TagRepository;
import com.zerobase.sns.domain.user.entity.User;
import com.zerobase.sns.domain.user.repository.UserRepository;
import com.zerobase.sns.global.exception.CustomException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final AlarmRepository alarmRepository;

  // 게시글 작성
  @Transactional
  public Post createPost(PostCreateDTO postCreateDTO, Principal principal) {
    String userId = principal.getName();
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

    // 닉네임으로 유저 태그
    List<Tag> tags = new ArrayList<>();

    if (postCreateDTO.getTags() != null) {
      for (String nickName : postCreateDTO.getTags()) {
        User taggedUser = userRepository.findByNickName(nickName)
            .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        Tag tag = Tag.builder()
            .user(taggedUser)
            .build();

        tags.add(tag);
      }
    }

    Post post = Post.builder()
        .user(user)
        .title(postCreateDTO.getTitle())
        .content(postCreateDTO.getContent())
        .tags(tags)
        .build();

    Post savedPost = postRepository.save(post);

    // 알람 생성
    for (Tag tag : tags) {
      createAlarm(user, tag.getUser(), savedPost, tag);
      tag.setPost(savedPost);
      tagRepository.save(tag);
    }

    return savedPost;
  }

  // 게시글 수정
  @Transactional
  public Post updatePost(Long postId, PostUpdateDTO postUpdateDTO, Principal principal) {
    String userId = principal.getName();
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_POST));

    if (!post.getUser().equals(user)) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    String title = Optional.ofNullable(postUpdateDTO.getTitle()).orElse(post.getTitle());
    String content = Optional.ofNullable(postUpdateDTO.getContent()).orElse(post.getContent());

    post.setTitle(title);
    post.setContent(content);

    return postRepository.save(post);
  }

  // 게시글 삭제
  @Transactional
  public void deletePost(Long postId, Principal principal) {
    String userId = principal.getName();
    User user = userRepository.findByUserId(userId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_USER));

    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new CustomException(NOT_FOUND_POST));

    if (!post.getUser().equals(user)) {
      throw new CustomException(UNAUTHORIZED_ACCESS);
    }

    postRepository.delete(post);
  }

  private void createAlarm(User tagger, User taggedUser, Post post, Tag tag) {
    String content = tagger.getNickName() + "님이 당신을 '" + post.getTitle() + "' 글에 태그했습니다.";
    Alarm alarm = Alarm.builder()
        .content(content)
        .user(taggedUser)
        .tag(tag)
        .build();
    alarmRepository.save(alarm);
  }
}

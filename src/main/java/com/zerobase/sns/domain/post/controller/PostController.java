package com.zerobase.sns.domain.post.controller;

import com.zerobase.sns.domain.post.dto.PostCreateDTO;
import com.zerobase.sns.domain.post.dto.PostDTO;
import com.zerobase.sns.domain.post.dto.PostUpdateDTO;
import com.zerobase.sns.domain.post.entity.Post;
import com.zerobase.sns.domain.post.service.PostService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/post")
public class PostController {

  private final PostService postService;

  // 게시글 작성
  @PostMapping
  public ResponseEntity<PostDTO> createPost(@RequestBody PostCreateDTO postCreateDTO,
      Principal principal) {

    Post post = postService.createPost(postCreateDTO, principal);

    return ResponseEntity.ok(PostDTO.convertToDTO(post));
  }

  // 게시글 수정
  @PutMapping("/{postId}")
  public ResponseEntity<PostDTO> updatePost(
      @PathVariable Long postId,
      @RequestBody PostUpdateDTO postUpdateDTO,
      Principal principal
  ) {
    Post updatedPost = postService.updatePost(postId, postUpdateDTO, principal);

    return ResponseEntity.ok(PostDTO.convertToDTO(updatedPost));
  }

  // 게시글 삭제
  @DeleteMapping("/{postId}")
  public void deletePost(@PathVariable Long postId, Principal principal) {
    postService.deletePost(postId, principal);
  }
}

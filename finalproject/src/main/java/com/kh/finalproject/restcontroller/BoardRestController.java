package com.kh.finalproject.restcontroller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.finalproject.dao.BoardDao;
import com.kh.finalproject.dto.AttachmentDto;
import com.kh.finalproject.dto.BoardDto;
import com.kh.finalproject.error.TargetNotfoundException;
import com.kh.finalproject.service.AttachmentService;

@CrossOrigin
@RestController
@RequestMapping("/board")
public class BoardRestController {

	@Autowired
	private BoardDao boardDao;
	@Autowired
	private AttachmentService attachmentService;
	
	// 게시글 등록
	@PostMapping("/")
	public void insert(@RequestBody BoardDto boardDto) {
		int boardNo =  boardDao.insert(boardDto);
		if(boardDto.getAttachmentNoList() != null) {
			for(int attachmentNo : boardDto.getAttachmentNoList()) {
				boardDao.connect(boardNo, attachmentNo);
			}
		}
	}
	
	//글 등록 전에 미리 이미지를 업로드하는 매핑 (임시이미지)
	@PostMapping("/temp")
	public int temp(@RequestParam MultipartFile attach) throws IllegalStateException, IOException {
		if(attach.isEmpty()) {
			throw new TargetNotfoundException("파일이 없습니다");
		}
		return attachmentService.save(attach);
	}
	
//	@PostMapping("/temps")
//	public List<Integer> temps(
//			@RequestParam(value = "attach") List<MultipartFile> attachList) throws IllegalStateException, IOException {
//		List<Integer> numbers = new ArrayList<>();
//		for(MultipartFile attach : attachList) {
//			if(attach.isEmpty() == false) {
//				int attachmentNo = attachmentService.save(attach);
//				numbers.add(attachmentNo);
//			}
//		}
//		return numbers;
//	}
	

	
	//전체 조회
	@GetMapping("/")
	public List<BoardDto> selectList(){
		return boardDao.selectList();
	}
		
	//상세 조회
	@GetMapping("/{boardNo}")
	public BoardDto selectOne(@PathVariable int boardNo) {
		return boardDao.selectOne(boardNo);
	}
	
	// 컨텐츠별 조회
	@GetMapping("/contentsId/{contentsId}")
	public List<BoardDto> selesctListByContents(@PathVariable long contentsId){
		return boardDao.selesctListByContents(contentsId);
	}
	// 컨텐츠별 5개 조회
	@GetMapping("/contentsId/{contentsId}/5")
	public List<BoardDto> selesctListBy5Contents(@PathVariable long contentsId){
		return boardDao.selesctListBy5Contents(contentsId);
	}
	
	
	// 게시글 삭제
	@DeleteMapping("/{boardNo}")
	public void delete(@PathVariable int boardNo) {
		boardDao.delete(boardNo);
	}
	
}

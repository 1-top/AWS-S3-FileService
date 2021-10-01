package com.harrybro.springkotlin.api

import com.harrybro.springkotlin.service.AwsS3Service
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/aws/s3")
class AwsS3ApiController(private val awsS3Service: AwsS3Service) {

    @PostMapping
    fun upload(@RequestParam file: MultipartFile) =
        this.awsS3Service.upload(file).let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping("/images/{filename}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun viewImage(@PathVariable filename: String) = this.awsS3Service.download(filename).let { ResponseEntity.ok(it) }

    @GetMapping("/{filename}")
    fun download(@PathVariable filename: String) = this.awsS3Service.download(filename)
        .let {
            ResponseEntity.ok().headers(HttpHeaders().apply {
                contentType = MediaType.APPLICATION_OCTET_STREAM
                contentDisposition = ContentDisposition.attachment().filename(filename).build()
            }).contentLength(it.size.toLong()).body(it)
        }

    @DeleteMapping("/{filename}")
    fun delete(@PathVariable filename: String) =
        this.awsS3Service.delete(filename).let { ResponseEntity.ok("The file deletion was successful.") }

}
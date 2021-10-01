package com.harrybro.springkotlin.service

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.util.*

interface AwsS3Service {
    fun upload(file: MultipartFile): String
    fun download(filename: String): ByteArray
    fun delete(filename: String)
}

@Service
class AwsS3ServiceImpl(
    @Value("\${cloud.aws.s3.bucket.name}") private val bucketName: String,
    private val amazonS3: AmazonS3
) : AwsS3Service {
    override fun upload(file: MultipartFile) = if (!file.isEmpty) {
        fun createPutObjectRequest(filename: String) = PutObjectRequest(
            this.bucketName,
            filename,
            file.inputStream,
            ObjectMetadata().apply {
                addUserMetadata("Content-Type", file.contentType)
                addUserMetadata("Content-Length", file.size.toString())
            }
        )
        val filename = UUID.randomUUID().toString() + "-" + file.originalFilename
        this.amazonS3.putObject(createPutObjectRequest(filename))
        filename
    } else {
        throw FileNotFoundException("The file does not exist.")
    }

    override fun download(filename: String): ByteArray {
        this.validateFileExist(filename)
        return this.amazonS3.getObject(this.bucketName, filename).objectContent.readBytes()
    }

    override fun delete(filename: String) {
        validateFileExist(filename)
        this.amazonS3.deleteObject(this.bucketName, filename)
    }

    private fun validateFileExist(filename: String) {
        if (!this.amazonS3.doesObjectExist(this.bucketName, filename))
            throw FileNotFoundException("The file does not exist.")
    }
}
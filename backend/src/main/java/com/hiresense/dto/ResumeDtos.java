package com.hiresense.dto;
import java.math.BigDecimal; import java.util.List;
public final class ResumeDtos { private ResumeDtos() {}
 public record ResumeResponse(Long id,String fileName,String contentType,long fileSize,String status,Long parsedDataId,String extractedEmail,BigDecimal yearsExperience,List<String> skills) {}
}

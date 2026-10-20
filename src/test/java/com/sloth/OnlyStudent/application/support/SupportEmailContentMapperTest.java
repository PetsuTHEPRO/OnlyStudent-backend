package com.sloth.OnlyStudent.application.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.sloth.OnlyStudent.entities.DTO.SupportDTO;

class SupportEmailContentMapperTest {

    private final SupportEmailContentMapper mapper = new SupportEmailContentMapper();

    @Test
    void buildsSubjectAndEscapesEveryUserControlledHtmlField() {
        SupportDTO request = new SupportDTO("<script>name</script>", "person@example.com", "<b>phone</b>",
                "<b>reason</b>", "<img src=x onerror=alert(1)>");

        assertEquals("Requisição de Suporte - email: person@example.com", mapper.subject(request));
        String html = mapper.toHtml(request);
        assertTrue(html.contains("&lt;script&gt;name&lt;/script&gt;"));
        assertTrue(html.contains("&lt;b&gt;phone&lt;/b&gt;"));
        assertTrue(html.contains("&lt;b&gt;reason&lt;/b&gt;"));
        assertTrue(html.contains("&lt;img src=x onerror=alert(1)&gt;"));
        assertFalse(html.contains("<script>"));
        assertFalse(html.contains("<img"));
    }
}

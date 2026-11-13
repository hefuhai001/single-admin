package com.hfh.api.modules.auth.controller;

import com.hfh.api.modules.auth.dto.EmailCodeDTO;
import com.hfh.api.modules.auth.dto.SliderVerifyDTO;
import com.hfh.api.modules.auth.service.CaptchaService;
import com.hfh.api.modules.auth.vo.SliderCaptchaVO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaptchaControllerTest {

    @Test
    void shouldDelegateAllEndpoints() {
        CaptchaService service = mock(CaptchaService.class);
        CaptchaController controller = new CaptchaController(service);
        SliderCaptchaVO vo = new SliderCaptchaVO();
        vo.setToken("token");
        when(service.generateSliderCaptcha()).thenReturn(vo);
        SliderVerifyDTO verifyDTO = new SliderVerifyDTO();
        when(service.verifySlider(verifyDTO)).thenReturn("verified");

        assertThat(controller.getSliderCaptcha().getData()).isSameAs(vo);
        assertThat(controller.verifySlider(verifyDTO).getData()).isEqualTo("verified");
        assertThat(controller.sendEmailCode(new EmailCodeDTO()).getCode()).isEqualTo(200);

        verify(service).sendEmailCode(org.mockito.ArgumentMatchers.any(EmailCodeDTO.class));
    }
}

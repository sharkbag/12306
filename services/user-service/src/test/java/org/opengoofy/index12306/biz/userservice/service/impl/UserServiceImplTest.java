/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.userservice.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserDeletionDO;
import org.opengoofy.index12306.biz.userservice.dao.entity.UserMailDO;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserDeletionMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMailMapper;
import org.opengoofy.index12306.biz.userservice.dao.mapper.UserMapper;
import org.opengoofy.index12306.biz.userservice.dto.req.UserUpdateReqDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryActualRespDTO;
import org.opengoofy.index12306.biz.userservice.dto.resp.UserQueryRespDTO;
import org.opengoofy.index12306.framework.starter.convention.exception.ClientException;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserDeletionMapper userDeletionMapper;

    @Mock
    private UserMailMapper userMailMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDO testUserDO;
    private UserQueryRespDTO testUserQueryRespDTO;
    private UserUpdateReqDTO testUpdateReqDTO;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUserDO = new UserDO();
        testUserDO.setId(1L);
        testUserDO.setUsername("testuser");
        testUserDO.setRealName("张三");
        testUserDO.setMail("test@example.com");
        testUserDO.setPhone("13800138000");
        testUserDO.setUserType(1);
        testUserDO.setPostCode("100000");
        testUserDO.setAddress("北京市朝阳区");

        testUserQueryRespDTO = new UserQueryRespDTO();
        testUserQueryRespDTO.setUsername("testuser");
        testUserQueryRespDTO.setRealName("张三");
        testUserQueryRespDTO.setMail("test@example.com");
        testUserQueryRespDTO.setPhone("13800138000");
        testUserQueryRespDTO.setUserType(1);
        testUserQueryRespDTO.setPostCode("100000");
        testUserQueryRespDTO.setAddress("北京市朝阳区");

        testUpdateReqDTO = new UserUpdateReqDTO();
        testUpdateReqDTO.setId("1");
        testUpdateReqDTO.setUsername("testuser");
        testUpdateReqDTO.setMail("newmail@example.com");
        testUpdateReqDTO.setUserType(2);
        testUpdateReqDTO.setPostCode("200000");
        testUpdateReqDTO.setAddress("上海市浦东新区");
    }

    /**
     * 测试根据用户名查询用户 - 成功场景
     */
    @Test
    void testQueryUserByUsername_Success() {
        // Given
        String username = "testuser";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUserDO);

        // When
        UserQueryRespDTO result = userService.queryUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("张三", result.getRealName());
        assertEquals("test@example.com", result.getMail());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试根据用户名查询用户 - 用户不存在
     */
    @Test
    void testQueryUserByUsername_UserNotFound() {
        // Given
        String username = "nonexistent";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        ClientException exception = assertThrows(ClientException.class, () -> {
            userService.queryUserByUsername(username);
        });
        assertEquals("用户不存在，请检查用户名是否正确", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试根据用户ID查询用户 - 成功场景
     */
    @Test
    void testQueryUserByUserId_Success() {
        // Given
        String userId = "1";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUserDO);

        // When
        UserQueryRespDTO result = userService.queryUserByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("张三", result.getRealName());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试根据用户ID查询用户 - 用户不存在
     */
    @Test
    void testQueryUserByUserId_UserNotFound() {
        // Given
        String userId = "999";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        ClientException exception = assertThrows(ClientException.class, () -> {
            userService.queryUserByUserId(userId);
        });
        assertEquals("用户不存在，请检查用户ID是否正确", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试更新用户信息 - 邮箱变更场景
     */
    @Test
    void testUpdate_EmailChanged() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUserDO);
        when(userMapper.update(any(UserDO.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // When
        userService.update(testUpdateReqDTO);

        // Then
        // 验证查询了用户
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        
        // 验证更新了用户信息
        ArgumentCaptor<UserDO> userCaptor = ArgumentCaptor.forClass(UserDO.class);
        ArgumentCaptor<LambdaUpdateWrapper> updateWrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
        verify(userMapper, times(1)).update(userCaptor.capture(), updateWrapperCaptor.capture());
        
        // 验证删除了旧邮箱记录
        verify(userMailMapper, times(1)).delete(any(LambdaUpdateWrapper.class));
        
        // 验证插入了新邮箱记录
        ArgumentCaptor<UserMailDO> mailCaptor = ArgumentCaptor.forClass(UserMailDO.class);
        verify(userMailMapper, times(1)).insert(mailCaptor.capture());
        
        UserMailDO insertedMail = mailCaptor.getValue();
        assertEquals("newmail@example.com", insertedMail.getMail());
        assertEquals("testuser", insertedMail.getUsername());
    }

    /**
     * 测试更新用户信息 - 邮箱未变更场景
     */
    @Test
    void testUpdate_EmailNotChanged() {
        // Given
        testUpdateReqDTO.setMail("test@example.com"); // 使用相同的邮箱
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUserDO);
        when(userMapper.update(any(UserDO.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // When
        userService.update(testUpdateReqDTO);

        // Then
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).update(any(UserDO.class), any(LambdaUpdateWrapper.class));
        
        // 验证没有操作邮箱记录
        verify(userMailMapper, never()).delete(any(LambdaUpdateWrapper.class));
        verify(userMailMapper, never()).insert(any(UserMailDO.class));
    }

    /**
     * 测试更新用户信息 - 邮箱为空场景
     */
    @Test
    void testUpdate_EmailBlank() {
        // Given
        testUpdateReqDTO.setMail(""); // 空邮箱
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUserDO);
        when(userMapper.update(any(UserDO.class), any(LambdaUpdateWrapper.class))).thenReturn(1);

        // When
        userService.update(testUpdateReqDTO);

        // Then
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, times(1)).update(any(UserDO.class), any(LambdaUpdateWrapper.class));
        
        // 验证没有操作邮箱记录
        verify(userMailMapper, never()).delete(any(LambdaUpdateWrapper.class));
        verify(userMailMapper, never()).insert(any(UserMailDO.class));
    }

    /**
     * 测试更新用户信息 - 用户不存在时抛出异常
     */
    @Test
    void testUpdate_UserNotFound() {
        // Given
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When & Then
        assertThrows(ClientException.class, () -> {
            userService.update(testUpdateReqDTO);
        });
        
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(userMapper, never()).update(any(UserDO.class), any(LambdaUpdateWrapper.class));
    }

    /**
     * 测试查询实际用户信息 - 成功场景
     */
    @Test
    void testQueryActualUserByUsername_Success() {
        // Given
        String username = "testuser";
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUserDO);

        // When
        UserQueryActualRespDTO result = userService.queryActualUserByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("张三", result.getRealName());
        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试查询用户注销数量 - 存在注销记录
     */
    @Test
    void testQueryUserDeletionNum_HasDeletionRecords() {
        // Given
        Integer idType = 1;
        String idCard = "110101199001011234";
        when(userDeletionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(2L);

        // When
        Integer result = userService.queryUserDeletionNum(idType, idCard);

        // Then
        assertNotNull(result);
        assertEquals(2, result.intValue());
        verify(userDeletionMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试查询用户注销数量 - 无注销记录
     */
    @Test
    void testQueryUserDeletionNum_NoDeletionRecords() {
        // Given
        Integer idType = 1;
        String idCard = "110101199001011234";
        when(userDeletionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // When
        Integer result = userService.queryUserDeletionNum(idType, idCard);

        // Then
        assertNotNull(result);
        assertEquals(0, result.intValue());
        verify(userDeletionMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }

    /**
     * 测试查询用户注销数量 - 返回null场景
     */
    @Test
    void testQueryUserDeletionNum_NullResult() {
        // Given
        Integer idType = 1;
        String idCard = "110101199001011234";
        when(userDeletionMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(null);

        // When
        Integer result = userService.queryUserDeletionNum(idType, idCard);

        // Then
        assertNotNull(result);
        assertEquals(0, result.intValue());
        verify(userDeletionMapper, times(1)).selectCount(any(LambdaQueryWrapper.class));
    }
}

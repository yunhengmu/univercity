/**
 * 导航工具函数 - 提取自 index.vue
 * 包含导航、语音播报、HTTP 请求等公共逻辑
 */

import config from '@/config'

// 从配置文件获取后端地址
const baseUrl = config.baseUrl

// ==================== 语音播报相关 ====================

/**
 * 创建语音播报实例
 * @param {string} text - 要播报的文本
 * @param {object} callbacks - 回调函数集合
 * @returns {SpeechSynthesisUtterance}
 */
export function createSpeechUtterance(text, callbacks = {}) {
    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'zh-CN';

    utterance.onstart = () => {
        if (callbacks.onStart) callbacks.onStart();
    };

    utterance.onend = () => {
        if (callbacks.onEnd) callbacks.onEnd();
    };

    utterance.onerror = (event) => {
        if (callbacks.onError) callbacks.onError(event);
    };

    utterance.onboundary = (event) => {
        if (event.name === 'word' && callbacks.onBoundary) {
            callbacks.onBoundary(event.charIndex);
        }
    };

    return utterance;
}

/**
 * 执行语音播报
 * @param {SpeechSynthesisUtterance} utterance - 语音实例
 * @param {object} options - 选项
 */
export function performSpeech(utterance, options = {}) {
    const { onError } = options;
    try {
        speechSynthesis.speak(utterance);
    } catch (error) {
        uni.showToast({ title: `语音播报失败`, icon: 'none', duration: 3000 });
        if (onError) onError(error);
    }
}

/**
 * 处理非H5环境的语音播报（微信小程序）
 * 使用微信同声传译插件进行TTS
 * @param {string} text - 要播报的文本
 * @param {function} callback - 完成后的回调
 * @param {object} context - Vue实例上下文（可选）
 */
export function handleNonH5Speech(text, callback, context = null) {
    // #ifdef MP-WEIXIN
    console.log('[TTS] 📱 微信小程序环境，准备播报');
    console.log('[TTS] 📝 文本内容:', text ? text.substring(0, 50) : 'null');
    console.log('[TTS] 📏 文本长度:', text ? text.length : 0);
    console.log('[TTS] 🔍 Context 是否存在:', !!context);
    console.log('[TTS] 🔍 Callback 类型:', typeof callback);
    
    // 检查文本是否为空
    if (!text || text.trim() === '') {
        console.warn('[TTS] ⚠️ 文本为空，跳过播报');
        if (callback) callback();
        return;
    }
    
    // 如果文本太长，分段处理（微信同声传译插件限制）
    const MAX_TEXT_LENGTH = 300; // 降低阈值，避免长文本导致 TTS 失败
    if (text.length > MAX_TEXT_LENGTH) {
        console.log('[TTS] ⚠️ 文本过长 (' + text.length + ' 字符)，分段处理');
        
        // 如果 context 有 startLongTextPlayback 方法，调用它来启动分段播报
        if (context && typeof context.startLongTextPlayback === 'function') {
            console.log('[TTS] 🔄 调用 context.startLongTextPlayback 启动分段播报');
            context.startLongTextPlayback(text, callback);
            return;
        }
        
        // 否则使用旧的分段逻辑（兼容）
        splitAndSpeak(text, callback, context);
        return;
    }
    
    // 方案1：尝试使用微信同声传译插件
    try {
        console.log('[TTS] 🔌 尝试加载微信同声传译插件...');
        const plugin = requirePlugin("WechatSI");
        console.log('[TTS] 🔍 插件对象:', plugin ? '存在' : '不存在');
        console.log('[TTS] 🔍 textToSpeech 方法:', plugin && plugin.textToSpeech ? '存在' : '不存在');
        
        if (plugin && plugin.textToSpeech) {
            console.log('[TTS] ✅ 插件可用，开始调用 textToSpeech');
            plugin.textToSpeech({
                lang: "zh_CN",
                content: text,
                success: function(res) {
                    console.log("[TTS] ✓ 转换成功", res);
                    
                    // 🔧 关键修复：微信 TTS 插件返回的字段是 voice_file，不是 filename
                    const audioUrl = res.voice_file || res.filename;
                    if (!audioUrl) {
                        console.error('[TTS] ❌ 未找到音频 URL，res 对象:', res);
                        fallbackSpeech(text, callback);
                        return;
                    }
                    
                    console.log("[TTS] 🎵 音频文件路径:", audioUrl);
                    
                    // 如果提供了 context，使用统一的音频管理
                    if (context && context.audioContext) {
                        // 停止之前的音频
                        try {
                            context.audioContext.stop();
                            console.log('[TTS] ⏹ 已停止旧音频');
                        } catch (e) {
                            console.warn('[TTS] 停止旧音频失败', e);
                        }
                    }
                    
                    // 创建或复用音频上下文
                    let audioContext;
                    if (context) {
                        // 销毁旧的音频上下文
                        if (context.audioContext) {
                            try {
                                context.audioContext.destroy();
                                console.log('[TTS] 🗑 已销毁旧音频上下文');
                            } catch (e) {
                                console.warn('[TTS] 销毁旧音频上下文失败', e);
                            }
                        }
                        // 创建新的音频上下文
                        audioContext = uni.createInnerAudioContext();
                        context.audioContext = audioContext;
                        console.log('[TTS] 🆕 创建新音频上下文并保存到 context');
                    } else {
                        audioContext = uni.createInnerAudioContext();
                        console.log('[TTS] 🆕 创建临时音频上下文');
                    }
                    
                    // 🔧 关键设置：确保音频能正常播放
                    audioContext.autoplay = false;  // 不自动播放，手动控制
                    audioContext.obeyMuteSwitch = false;  // 忽略系统静音开关
                    audioContext.volume = 1.0;  // 设置音量为最大
                    
                    console.log('[TTS] 🔊 音量设置为:', audioContext.volume);
                    console.log('[TTS] 🔇 忽略静音开关:', audioContext.obeyMuteSwitch);
                    
                    // 🔧 关键修复：使用正确的音频URL字段
                    audioContext.src = audioUrl;
                    console.log('[TTS] 🎵 设置音频源:', audioUrl);
                    
                    // 监听音频可以播放的事件
                    audioContext.onCanplay(() => {
                        console.log('[TTS] ✅ 音频已加载，可以播放');
                    });
                    
                    // 监听播放开始
                    audioContext.onPlay(() => {
                        console.log('[TTS] ▶️ 音频开始播放');
                    });
                    
                    // 监听播放结束
                    audioContext.onEnded(() => {
                        console.log('[TTS] ✅ 播放完成');
                        audioContext.destroy();
                        // 如果使用了 context，清空引用
                        if (context && context.audioContext === audioContext) {
                            context.audioContext = null;
                            console.log('[TTS] 🗑 已清空 context.audioContext');
                        }
                        if (callback) {
                            console.log('[TTS] 🔄 调用回调函数');
                            callback();
                        }
                    });
                    
                    // 监听播放错误
                    audioContext.onError((err) => {
                        console.error("[TTS] ✗ 音频播放失败", err);
                        audioContext.destroy();
                        // 如果使用了 context，清空引用
                        if (context && context.audioContext === audioContext) {
                            context.audioContext = null;
                        }
                        // 降级方案
                        console.log('[TTS] ⬇️ 使用降级方案');
                        fallbackSpeech(text, callback);
                    });
                    
                    // 开始播放
                    console.log('[TTS] ▶ 准备播放，音频源:', audioContext.src);
                    
                    // 延迟一小段时间再播放，确保音频已加载
                    setTimeout(() => {
                        console.log('[TTS] ▶ 调用 audioContext.play()');
                        audioContext.play();
                        console.log('[TTS] ▶ play() 方法已调用');
                    }, 100);
                },
                fail: function(err) {
                    console.log("[TTS] ✗ 转换失败，使用降级方案", err);
                    console.log("[TTS] ❌ 错误码:", err.retcode, "错误信息:", err.msg);
                    // 降级方案：显示 Toast
                    fallbackSpeech(text, callback);
                }
            });
            console.log('[TTS] ✅ textToSpeech 已调用，等待响应...');
            return;  // 重要：返回，不执行后面的降级代码
        } else {
            console.warn('[TTS] ⚠️ 插件或 textToSpeech 方法不存在');
            // 插件不可用，执行降级
            fallbackSpeech(text, callback);
        }
    } catch (e) {
        console.error("[TTS] ✗ 微信同声传译插件异常", e);
        // 发生异常，执行降级
        fallbackSpeech(text, callback);
    }
    // #endif
    
    // #ifndef MP-WEIXIN
    uni.showToast({ title: `语音：${text.substring(0, 20)}...`, icon: 'none', duration: 3000 });
    setTimeout(() => {
        if (callback) callback();
    }, 3000);
    // #endif
}

/**
 * 将长文本分段并依次播报
 * @param {string} text - 长文本
 * @param {function} callback - 全部完成后的回调
 * @param {object} context - Vue实例上下文
 */
function splitAndSpeak(text, callback, context) {
    console.log('[TTS 分段] ========== 开始分段处理 ==========');
    const MAX_LENGTH = 300; // 降低分段长度，避免微信TTS插件处理失败
    const segments = [];
    
    console.log('[TTS 分段] 📏 原始文本长度:', text.length);
    console.log('[TTS 分段] 📐 分段长度限制:', MAX_LENGTH);
    
    // 按句子分割（优先在句号、问号、感叹号处分割）
    const sentences = text.match(/[^。！？.!?]+[。！？.!?]+/g) || [text];
    console.log('[TTS 分段] 📝 句子数量:', sentences.length);
    console.log('[TTS 分段] 📋 原始文本预览:', text.substring(0, 100) + '...');
    sentences.forEach((s, idx) => {
        console.log(`[TTS 分段]   句子${idx + 1}:`, s.substring(0, 50));
    });
    
    let currentSegment = '';
    sentences.forEach(sentence => {
        if ((currentSegment + sentence).length > MAX_LENGTH && currentSegment) {
            // 清理文本，去除首尾空白
            const cleanedSegment = currentSegment.trim();
            if (cleanedSegment.length > 0) {
                segments.push(cleanedSegment);
            }
            currentSegment = sentence;
        } else {
            currentSegment += sentence;
        }
    });
    if (currentSegment) {
        // 清理最后一段文本
        const cleanedSegment = currentSegment.trim();
        if (cleanedSegment.length > 0) {
            segments.push(cleanedSegment);
        }
    }
    
    console.log('[TTS 分段] 📊 共分为', segments.length, '段');
    segments.forEach((seg, idx) => {
        console.log(`[TTS 分段]   第${idx + 1}段长度:`, seg.length);
        console.log(`[TTS 分段]   第${idx + 1}段内容:`, seg.substring(0, 80) + '...');
    });
    console.log('[TTS 分段] 🔍 当前 context.audioContext 是否存在:', !!(context && context.audioContext));
    console.log('[TTS 分段] ========== 分段处理完成，准备播报 ==========');
    
    // 重要：将分段数组保存到 context 中
    if (context) {
        context.currentLongTextSegments = segments;
        context.currentSegmentIndex = 0;
        console.log('[TTS 分段] 💾 已保存分段数组到 context，长度:', segments.length);
    }
    
    // 递归播报每一段
    let currentIndex = 0;
    const speakNext = () => {
        console.log('[TTS 分段] 🔍 speakNext 被调用, currentIndex:', currentIndex, 'segments.length:', segments.length);
        
        if (currentIndex >= segments.length) {
            console.log('[TTS 分段] ========== 所有段落播报完成 ==========');
            console.log('[TTS 分段] ✅ 所有段落播报完成');
            console.log('[TTS 分段] 🔍 callback 是否存在:', !!callback);
            console.log('[TTS 分段] 🔍 callback 类型:', typeof callback);
            
            // 所有分段都播放完毕，调用最终回调
            if (callback && typeof callback === 'function') {
                console.log('[TTS 分段] 🔄 开始调用最终 callback');
                try {
                    callback();
                    console.log('[TTS 分段] ✅ callback 调用成功');
                } catch (error) {
                    console.error('[TTS 分段] ❌ callback 调用失败:', error);
                }
            } else {
                console.warn('[TTS 分段] ⚠️ callback 不存在或不是函数，跳过调用');
            }
            return;
        }
        
        const segment = segments[currentIndex];
        console.log('[TTS 分段] 🎤 播报第', currentIndex + 1, '/', segments.length, '段，长度:', segment.length);
        console.log('[TTS 分段] 📝 第', currentIndex + 1, '段内容:', segment.substring(0, 100) + (segment.length > 100 ? '...' : ''));
        
        // 调用 TTS，但不传 callback，由我们自己控制下一段
        handleSingleSegment(segment, context, () => {
            console.log('[TTS 分段] 🔄 第', currentIndex + 1, '段完成，准备下一段');
            currentIndex++;
            // 延迟一点再播放下一段，避免切换太快（减少到200ms，更流畅）
            setTimeout(() => {
                speakNext();
            }, 200);
        });
    };
    
    // 重要：不再直接调用 speakNext()，而是让 Vue 实例的 handleSegmentComplete 来处理
    // 这样可以确保使用统一的索引管理逻辑
    console.log('[TTS 分段] ℹ️ 分段已保存到 context，等待 Vue 实例处理');
    // 不调用 speakNext()，让 index.vue 中的逻辑来处理
}

/**
 * 播报单个文本段
 * @param {string} text - 文本段
 * @param {object} context - Vue实例上下文
 * @param {function} onComplete - 完成回调
 */
function handleSingleSegment(text, context, onComplete) {
    try {
        // 验证文本是否有效
        if (!text || typeof text !== 'string' || text.trim().length === 0) {
            console.warn('[TTS 分段] ⚠️ 文本无效或为空，跳过处理');
            if (onComplete) onComplete();
            return;
        }
        
        // 清理文本：移除不可见字符、标准化可能导致解析错误的标点符号
        let cleanedText = text
            .replace(/[\s\u200B-\u200D\uFEFF]+/g, ' ')  // 移除空白和不可见字符
            .replace(/["""]/g, '"')  // 中文双引号（左右）转英文
            .replace(/['''']/g, "'")  // 中文单引号（左右）转英文
            .replace(/：/g, ':')       // 中文冒号转英文（可能引起解析错误）
            .replace(/——/g, '-')     // 中文破折号转英文
            .replace(/……/g, '...')   // 中文省略号转英文
            // 注意：保留中文逗号、句号等，因为 TTS 需要它们来正确断句
            .trim();
        
        if (cleanedText.length === 0) {
            console.warn('[TTS 分段] ⚠️ 清理后文本为空，跳过处理');
            if (onComplete) onComplete();
            return;
        }
        
        const plugin = requirePlugin("WechatSI");
        if (plugin && plugin.textToSpeech) {
            console.log('[TTS 分段] 🔄 开始转换，原始长度:', text.length, '清理后长度:', cleanedText.length);
            console.log('[TTS 分段] 📝 清理后预览:', cleanedText.substring(0, 80));
            
            // 检查是否包含中文引号（用于调试）
            if (text.match(/["""]/)) {
                console.warn('[TTS 分段] ⚠️ 检测到中文双引号，已转换为英文');
            }
            if (text.match(/['''']/)) {
                console.warn('[TTS 分段] ⚠️ 检测到中文单引号，已转换为英文');
            }
            if (text.match(/：/)) {
                console.warn('[TTS 分段] ⚠️ 检测到中文冒号，已转换为英文');
            }
            if (text.match(/——/)) {
                console.warn('[TTS 分段] ⚠️ 检测到中文破折号，已转换为英文');
            }
            
            plugin.textToSpeech({
                lang: "zh_CN",
                content: cleanedText,  // 使用清理后的文本
                success: function(res) {
                    console.log('[TTS 分段] ✓ 转换成功');
                    
                    let audioContext;
                    if (context) {
                        // 只有在已有音频上下文时才清理
                        if (context.audioContext) {
                            try {
                                console.log('[TTS 分段] ⚠️ 检测到旧的音频上下文，准备替换');
                                // 先停止旧音频（如果正在播放）
                                context.audioContext.stop();
                                console.log('[TTS 分段] ⏹ 已停止旧音频');
                            } catch (e) {
                                console.warn('[TTS 分段] 停止旧音频失败', e);
                            }
                            try {
                                // 再销毁旧音频上下文
                                context.audioContext.destroy();
                                console.log('[TTS 分段] 🗑 已销毁旧音频上下文');
                            } catch (e) {
                                console.warn('[TTS 分段] 销毁旧音频上下文失败', e);
                            }
                        }
                        // 创建新的音频上下文
                        audioContext = uni.createInnerAudioContext();
                        context.audioContext = audioContext;
                        console.log('[TTS 分段] 🆕 创建新音频上下文');
                    } else {
                        audioContext = uni.createInnerAudioContext();
                        console.log('[TTS 分段] 🆕 创建临时音频上下文');
                    }
                    
                    audioContext.src = res.filename;
                    console.log('[TTS 分段] 🎵 设置音频源');
                    
                    // 🔧 关键设置：确保音频能正常播放
                    audioContext.autoplay = false;  // 不自动播放，手动控制
                    audioContext.obeyMuteSwitch = false;  // 忽略系统静音开关
                    audioContext.volume = 1.0;  // 设置音量为最大
                    console.log('[TTS 分段] 🔊 音量设置为:', audioContext.volume);
                    console.log('[TTS 分段] 🔇 忽略静音开关:', audioContext.obeyMuteSwitch);
                    
                    // 监听音频可以播放的事件
                    audioContext.onCanplay(() => {
                        console.log('[TTS 分段] ✅ 音频已加载，可以播放');
                    });
                    
                    // 监听播放开始
                    audioContext.onPlay(() => {
                        console.log('[TTS 分段] ▶️ 音频开始播放');
                    });
                    
                    // 监听播放结束
                    audioContext.onEnded(() => {
                        console.log('[TTS 分段] ✅ 播放完成');
                        audioContext.destroy();
                        if (context && context.audioContext === audioContext) {
                            context.audioContext = null;
                            console.log('[TTS 分段] 🗑 已清空音频上下文引用');
                        }
                        if (onComplete) {
                            console.log('[TTS 分段] 🔄 调用完成回调');
                            onComplete();
                        }
                    });
                    
                    // 监听播放错误 - 增强错误处理
                    audioContext.onError((err) => {
                        console.error('[TTS 分段] ✗ 播放失败', err);
                        console.error('[TTS 分段] ❌ 错误码:', err.errCode, '错误信息:', err.errMsg);
                        
                        // 销毁音频上下文
                        try {
                            audioContext.destroy();
                        } catch (e) {
                            console.warn('[TTS 分段] 销毁音频上下文失败', e);
                        }
                        
                        if (context && context.audioContext === audioContext) {
                            context.audioContext = null;
                        }
                        
                        // 即使失败也继续下一段，避免卡住
                        console.log('[TTS 分段] ⚠️ 播放失败，继续下一段');
                        if (onComplete) onComplete();
                    });
                    
                    // 监听暂停事件（用户可能调整音量）
                    audioContext.onPause(() => {
                        console.log('[TTS 分段] ⏸ 音频被暂停（可能是音量调节）');
                    });
                    
                    // 监听停止事件
                    audioContext.onStop(() => {
                        console.log('[TTS 分段] ⏹ 音频被停止');
                    });
                    
                    // 开始播放
                    console.log('[TTS 分段] ▶ 准备播放，音频源:', audioContext.src);
                    
                    // 延迟一小段时间再播放，确保音频已加载
                    setTimeout(() => {
                        console.log('[TTS 分段] ▶ 调用 audioContext.play()');
                        audioContext.play();
                        console.log('[TTS 分段] ▶ play() 方法已调用');
                    }, 100);
                },
                fail: function(err) {
                    console.error('[TTS 分段] ✗ 转换失败', err);
                    console.error('[TTS 分段] ❌ 错误码:', err.retcode, '错误信息:', err.msg);
                    console.error('[TTS 分段] 🔍 问题文本预览:', text.substring(0, 100));
                    
                    // 根据错误码提供建议
                    if (err.retcode === -20003) {
                        console.warn('[TTS 分段] ⚠️ 服务器内部错误，可能是文本包含特殊字符或标点符号');
                        console.warn('[TTS 分段] 💡 建议：检查文本中是否有中文引号、破折号等特殊标点');
                    } else if (err.retcode === -20) {
                        console.warn('[TTS 分段] ⚠️ 非法请求，文本可能为空或格式错误');
                    }
                    
                    // 转换失败也继续下一段
                    if (onComplete) {
                        console.log('[TTS 分段] ⚠️ 转换失败，继续下一段');
                        onComplete();
                    }
                }
            });
        } else {
            console.warn('[TTS 分段] ⚠️ 插件不可用');
            if (onComplete) onComplete();
        }
    } catch (e) {
        console.error('[TTS 分段] ✗ 异常', e);
        console.error('[TTS 分段] ❌ 异常堆栈:', e.stack);
        if (onComplete) onComplete();
    }
}

/**
 * 降级语音播报方案
 * @param {string} text - 要播报的文本
 * @param {function} callback - 完成后的回调
 */
function fallbackSpeech(text, callback) {
    uni.showToast({ 
        title: `语音：${text.substring(0, 20)}...`, 
        icon: 'none', 
        duration: 3000 
    });
    setTimeout(() => {
        if (callback) callback();
    }, 3000);
}

// ==================== HTTP 请求相关 ====================

/**
 * 发送 HTTP 请求的通用函数
 * @param {object} options - 请求配置项
 * @returns {Promise}
 */
export function sendRequest(options) {
    const {
        url,
        method = 'GET',
        data,
        timeout = 480000,
        needCheckNavigating = true,
        timeoutFallback,
        requestTasksArray
    } = options;

    return new Promise((resolve, reject) => {
        if (needCheckNavigating && !options.context.isNavigating) {
            reject(new Error('导航已停止'));
            return;
        }

        const requestTask = uni.request({
            url: url,
            method: method,
            header: options.header || { 'Content-Type': 'application/json' },
            data: data,
            timeout: timeout,
            success: (res) => {
                // 从任务列表中移除
                if (requestTasksArray) {
                    const index = requestTasksArray.indexOf(requestTask);
                    if (index > -1) requestTasksArray.splice(index, 1);
                }

                if (needCheckNavigating && !options.context.isNavigating) {
                    reject(new Error('导航已停止'));
                    return;
                }

                if (res.statusCode >= 200 && res.statusCode < 300) {
                    resolve(res.data);
                } else {
                    reject(new Error(`HTTP 错误：${res.statusCode}`));
                }
            },
            fail: (err) => {
                // 从任务列表中移除
                if (requestTasksArray) {
                    const index = requestTasksArray.indexOf(requestTask);
                    if (index > -1) requestTasksArray.splice(index, 1);
                }

                if (err.errMsg.includes('timeout')) {
                    resolve(timeoutFallback);
                } else {
                    reject(new Error(`请求失败：${err.errMsg}`));
                }
            },
            complete: () => {
                // 请求完成
            }
        });

        // 添加到任务列表
        if (requestTasksArray) {
            requestTasksArray.push(requestTask);
        }
    });
}

/**
 * 获取用户文本
 * @param {object} navigationData - 导航数据
 * @param {object} context - Vue 实例上下文
 * @returns {Promise}
 */
export function getUserText(navigationData, context) {
    const backendUrl = `${baseUrl}/api/get-user`;
    return sendRequest({
        url: backendUrl,
        method: 'POST',
        header: { 'Content-Type': 'application/json', 'Accept': '*/*' },
        data: navigationData,
        timeout: 480000,
        context: context,
        requestTasksArray: context.requestTasks
    });
}

/**
 * 获取书籍 ID
 * @param {object} navigationData - 导航数据
 * @param {object} context - Vue 实例上下文
 * @returns {Promise}
 */
export function getBookId(navigationData, context) {
    const backendUrl = `${baseUrl}/api/start-navigation`;
    return sendRequest({
        url: backendUrl,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: navigationData,
        timeout: 480000,
        timeoutFallback: { bookId: 1, trajectoryId: 1 },
        context: context,
        requestTasksArray: context.requestTasks
    });
}

/**
 * 获取书籍 ID（不带书籍信息的导航）
 * @param {object} navigationData - 导航数据
 * @param {object} context - Vue 实例上下文
 * @returns {Promise}
 */
export function getBookIdNoBook(navigationData, context) {
    const backendUrl = `${baseUrl}/api/startNavigationNoBook`;
    return sendRequest({
        url: backendUrl,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: navigationData,
        timeout: 480000,
        timeoutFallback: { bookId: 1, trajectoryId: 1 },
        context: context,
        requestTasksArray: context.requestTasks
    });
}

/**
 * 获取章节文本
 * @param {object} params - 参数
 * @param {object} context - Vue 实例上下文
 * @returns {Promise}
 */
export function getChapterText(params, context) {
    const backendUrl = `${baseUrl}/api/get-chapter`;
    return sendRequest({
        url: backendUrl,
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        data: { bookId: params.bookId, trajectoryId: params.trajectoryId, length: params.length },
        timeout: 480000,
        timeoutFallback: '正在导航中...',
        context: context,
        requestTasksArray: context.requestTasks
    });
}

/**
 * 获取区域信息
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @param {string} cacheKey - 缓存键
 * @param {object} context - Vue 实例上下文
 * @returns {Promise}
 */
export function getAreaInfo(lng, lat, cacheKey = null, context) {
    let areaUrl = `${baseUrl}/api/getArea?longitude=${lng}&latitude=${lat}`;
    if (cacheKey) {
        areaUrl += `&cacheKey=${cacheKey}`;
    }

    return sendRequest({
        url: areaUrl,
        method: 'GET',
        timeout: 30000,
        needCheckNavigating: false,
        context: context,
        requestTasksArray: context.requestTasks
    });
}

/**
 * 获取 POI 信息
 * @param {number} lng - 经度
 * @param {number} lat - 纬度
 * @param {string} cacheKey - 缓存键
 * @param {object} context - Vue 实例上下文
 * @returns {Promise}
 */
export function getPoiInfo(lng, lat, cacheKey = null, context) {
    let areaUrl = `${baseUrl}/api/getPoi?longitude=${lng}&latitude=${lat}`;
    if (cacheKey) {
        areaUrl += `&cacheKey=${cacheKey}`;
    }

    return sendRequest({
        url: areaUrl,
        method: 'GET',
        timeout: 30000,
        needCheckNavigating: false,
        context: context,
        requestTasksArray: context.requestTasks
    });
}

// ==================== 清理和状态管理 ====================

/**
 * 清理所有定时器和监听器
 * @param {object} context - Vue 实例上下文
 */
export function cleanupTimersAndWatchers(context) {
    if (context.timer) {
        clearInterval(context.timer);
        context.timer = null;
    }
    if (context.culturalTimer) {
        clearInterval(context.culturalTimer);
        context.culturalTimer = null;
        context.culturalTimestamps = [];
    }
    if (context.speechTimer) {
        clearTimeout(context.speechTimer);
        context.speechTimer = null;
    }
    if (context.positionWatcher) {
        navigator.geolocation.clearWatch(context.positionWatcher);
        context.positionWatcher = null;
    }
}

/**
 * 取消所有请求任务
 * @param {array} requestTasks - 请求任务列表
 */
export function cancelAllRequests(requestTasks) {
    if (requestTasks && requestTasks.length > 0) {
        requestTasks.forEach(task => {
            if (task && typeof task.abort === 'function') {
                try {
                    task.abort();
                } catch (e) { }
            }
        });
        requestTasks = [];
    }
}

/**
 * 清理语音相关状态
 * @param {object} context - Vue 实例上下文
 */
export function clearSpeechState(context) {
    context.currentSpeechQueue = [];
    context.currentSpeakingText = null;
    context.currentSpeechIndex = 0;
    context.speechTextQueue = [];
}

/**
 * 清理导航状态
 * @param {object} context - Vue 实例上下文
 */
export function clearNavigationState(context) {
    context.navigationParams = null;
    context.currentBookId = null;
    context.cacheKey = null;
    context.currentSpeed = 0;
    context.lastPosition = null;
    context.lastTimestamp = 0;
}

/**
 * 停止导航的完整流程
 * @param {object} context - Vue 实例上下文
 * @param {boolean} isCulturalMode - 是否为文化模式
 */
export function stopNavigation(context, isCulturalMode) {
    if (!context.isNavigating) return;

    context.isNavigating = false;
    context.isPaused = false;
    context.nextInstruction = "等待导航开始...";
    uni.showToast({ title: '导航已停止', icon: 'none' });

    // 取消所有请求
    cancelAllRequests(context.requestTasks);

    // 清理定时器和监听器
    cleanupTimersAndWatchers(context);

    // 停止 POI 轮询
    if (context.poiRequestTimer) {
        clearInterval(context.poiRequestTimer);
        context.poiRequestTimer = null;
    }
    context.isPoiRequesting = false;
    context.selectedPointLocation = null;

    // H5 平台取消语音合成
    // #ifdef H5
    if (window.speechSynthesis) window.speechSynthesis.cancel();
    // #endif
    
    // 微信小程序停止音频播放
    // #ifdef MP-WEIXIN
    if (context.audioContext) {
        context.audioContext.stop();
        context.audioContext.destroy();
        context.audioContext = null;
    }
    // #endif

    // 清理状态
    clearSpeechState(context);
    clearNavigationState(context);

    // 清理路径
    if (context.driving) context.driving.clear();

    // 恢复模式
    context.mode = isCulturalMode ? 1 : 0;
}

// ==================== 距离计算 ====================

/**
 * 计算两点之间的距离（Haversine 公式）
 * @param {number} lat1 - 起点纬度
 * @param {number} lon1 - 起点经度
 * @param {number} lat2 - 终点纬度
 * @param {number} lon2 - 终点经度
 * @returns {number} 距离（米）
 */
export function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // 地球半径（米）
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
        Math.cos(φ1) * Math.cos(φ2) *
        Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
}

// ==================== 速度检测 ====================

/**
 * 启动速度检测
 * @param {object} context - Vue 实例上下文
 * @param {number} speedLimitThreshold - 限速阈值
 */
export function startSpeedDetection(context, speedLimitThreshold) {
    // #ifdef H5
    if (navigator.geolocation) {
        context.positionWatcher = navigator.geolocation.watchPosition(
            (position) => {
                const currentTime = new Date().getTime();
                const currentCoords = position.coords;

                if (context.lastPosition && context.lastTimestamp) {
                    const distance = calculateDistance(
                        context.lastPosition.latitude,
                        context.lastPosition.longitude,
                        currentCoords.latitude,
                        currentCoords.longitude
                    );
                    const timeDiff = (currentTime - context.lastTimestamp) / 1000;

                    if (timeDiff > 0) {
                        const speed = (distance / timeDiff) * 3.6; // km/h
                        context.currentSpeed = Math.round(speed * 10) / 10;

                        // 检查是否超速
                        if (context.selectedSpeedLimit !== 'off' &&
                            context.currentSpeed > speedLimitThreshold) {
                            // 超速时暂停语音播报
                            return;
                        }
                    }
                }

                context.lastPosition = currentCoords;
                context.lastTimestamp = currentTime;
            },
            (error) => {
                // 速度检测错误，静默处理
            },
            { enableHighAccuracy: true, maximumAge: 1000 }
        );
    }
    // #endif
}
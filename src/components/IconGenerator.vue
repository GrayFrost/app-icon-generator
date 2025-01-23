<script setup>
import { ref } from "vue";
import JSZip from "jszip";
import { saveAs } from "file-saver";

const sizes = [16, 32, 64, 128, 256, 512, 1024];
const generatedImages = ref([]);
const processing = ref(false);

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  try {
    generateIcons(file);
  } catch (error) {
    console.error("图片处理失败:", error);
  } finally {
    processing.value = false;
  }
};

async function generateIcons(imageFile) {
    try {
        const formData = new FormData();
        formData.append('image', imageFile);
        const baseUrl = import.meta.env.BASE_URL;
        const response = await fetch(`${baseUrl}/api/icons/generate`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error('生成图标失败');
        }

        // 获取文件名
        const contentDisposition = response.headers.get('content-disposition');
        const fileName = contentDisposition
            ? contentDisposition.split('filename=')[1].replace(/"/g, '')
            : 'icons.zip';

        // 将响应转换为 blob
        const blob = await response.blob();
        
        // 创建下载链接
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        
        // 清理
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    } catch (error) {
        console.error('生成图标时出错:', error);
        throw error;
    }
}

const downloadZip = async () => {
  const zip = new JSZip();

  // 创建 icons 文件夹
  const iconsFolder = zip.folder("icons");

  // 添加所有图片到压缩包
  generatedImages.value.forEach(({ size, blob }) => {
    iconsFolder.file(`icon_${size}x${size}.png`, blob);
  });

  // 生成并下载压缩包
  const content = await zip.generateAsync({ type: "blob" });
  saveAs(content, "app_icons.zip");
};

const downloadSingle = ({ size, url }) => {
  const link = document.createElement("a");
  link.href = url;
  link.download = `icon_${size}x${size}.png`;
  link.click();
};
</script>

<template>
  <div class="icon-generator">
    <h2>macOS 应用图标生成器</h2>

    <div class="upload-section">
      <div class="upload-box" :class="{ processing, 'has-file': generatedImages.length }">
        <input 
          type="file" 
          accept="image/*" 
          @change="handleFileUpload"
          :disabled="processing"
          id="file-upload"
          class="file-input"
        />
        <label for="file-upload" class="file-label">
          <div class="upload-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
          </div>
          <div class="upload-text">
            <span class="primary-text">点击或拖拽文件到此处</span>
            <span class="secondary-text">支持 1024x1024 像素的 PNG 图片</span>
          </div>
        </label>
        <div v-if="processing" class="processing-indicator">
          <div class="spinner"></div>
          <span>正在处理...</span>
        </div>
      </div>
    </div>

    <div v-if="generatedImages.length" class="results">
      <div class="buttons">
        <button @click="downloadZip" class="primary">下载压缩包</button>
      </div>

      <div class="preview-grid">
        <div
          v-for="image in generatedImages"
          :key="image.size"
          class="preview-item"
        >
          <img :src="image.url" :alt="`${image.size}x${image.size}`" />
          <p>{{ image.size }}x{{ image.size }}</p>
          <button @click="() => downloadSingle(image)" class="small">
            下载
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.icon-generator {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}

.upload-section {
  margin: 2rem 0;
}

.upload-box {
  border: 2px dashed #666;
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  position: relative;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  width: 500px;
  margin: 0 auto;
}

.upload-box:hover {
  border-color: #646cff;
  background: rgba(100, 108, 255, 0.08);
}

.file-input {
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
  overflow: hidden;
  position: absolute;
  z-index: -1;
}

.file-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 2rem;
  min-height: 200px;
}

.upload-icon {
  margin-bottom: 1rem;
  color: #666;
  transition: color 0.3s ease;
}

.file-label:hover .upload-icon {
  color: #646cff;
}

.upload-text {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.primary-text {
  font-size: 1.2rem;
  font-weight: 500;
}

.secondary-text {
  font-size: 0.9rem;
  color: #888;
}

.processing {
  opacity: 0.7;
  pointer-events: none;
}

.processing-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 1rem 2rem;
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #ffffff;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 拖拽状态样式 */
.upload-box.drag-over {
  border-color: #646cff;
  background: rgba(100, 108, 255, 0.15);
}

/* 已有文件状态样式 */
.upload-box.has-file {
  border-style: solid;
  background: rgba(100, 108, 255, 0.05);
}

.results {
  margin-top: 2rem;
}

.buttons {
  margin-bottom: 2rem;
}

.preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.preview-item {
  text-align: center;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.preview-item img {
  max-width: 100%;
  height: auto;
}

button.primary {
  background-color: #646cff;
  color: white;
}

button.small {
  padding: 0.3em 0.6em;
  font-size: 0.9em;
  margin-top: 0.5rem;
}
</style>

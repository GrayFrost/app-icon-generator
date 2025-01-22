<script setup>
import { ref } from "vue";
import JSZip from "jszip";
import { saveAs } from "file-saver";

const sizes = [16, 32, 64, 128, 256, 512, 1024];
const generatedImages = ref([]);

const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  try {
    const img = new Image();
    img.src = URL.createObjectURL(file);

    await new Promise((resolve) => {
      img.onload = resolve;
    });

    const promises = sizes.map(async (size) => {
      const canvas = document.createElement("canvas");
      canvas.width = size;
      canvas.height = size;
      const ctx = canvas.getContext("2d");
      ctx.drawImage(img, 0, 0, size, size);

      return new Promise((resolve) => {
        canvas.toBlob((blob) => {
          resolve({
            size,
            blob,
            url: URL.createObjectURL(blob),
          });
        }, "image/png");
      });
    });

    generatedImages.value = await Promise.all(promises);
  } catch (error) {
    console.error("图片处理失败:", error);
  }
};

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
      <input type="file" accept="image/*" @change="handleFileUpload" />
      <p class="hint">请上传 1024x1024 像素的PNG图片</p>
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
}

.upload-section {
  margin: 2rem 0;
}

.hint {
  color: #666;
  font-size: 0.9rem;
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

import express from 'express';
import multer from 'multer';
import sharp from 'sharp';
import cors from 'cors';
import rateLimit from 'express-rate-limit';

const app = express();
app.use(cors());

const upload = multer({
  limits: {
    fileSize: 5 * 1024 * 1024 // 限制5MB
  }
});

const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15分钟时间窗口
  max: 100, // 每个 IP 在时间窗口内最多 100 次请求
  message: { error: '请求过于频繁，请稍后再试' }
});

// 应用限流中间件
app.use('/generate-icons', limiter);

const validateImage = (req, res, next) => {
  if (!req.file) {
    return res.status(400).json({ error: '请上传文件' });
  }

  // 验证文件类型
  if (!req.file.mimetype.startsWith('image/')) {
    return res.status(400).json({ error: '只允许上传图片文件' });
  }

  // 验证文件大小（已在 multer 中设置，这里是双重检查）
  if (req.file.size > 5 * 1024 * 1024) {
    return res.status(400).json({ error: '文件大小不能超过 5MB' });
  }

  next();
};

app.post('/generate-icons', upload.single('icon'), validateImage, async (req, res) => {
  try {
    const sizes = [16, 32, 64, 128, 256, 512, 1024];
    const image = sharp(req.file.buffer);
    
    // 验证图片尺寸
    const metadata = await image.metadata();
    if (metadata.width !== 1024 || metadata.height !== 1024) {
      return res.status(400).json({ error: '请上传 1024x1024 像素的图片' });
    }
    
    const results = await Promise.all(
      sizes.map(async (size) => {
        const buffer = await image
          .clone()
          .resize(size, size, {
            kernel: sharp.kernel.lanczos3,
            fit: 'contain',
            background: { r: 0, g: 0, b: 0, alpha: 0 }
          })
          .sharpen({
            sigma: size < 64 ? 1 + (64 - size) / 64 : 1
          })
          .png({ quality: 100 })
          .toBuffer();
          
        return {
          size,
          buffer: buffer.toString('base64')
        };
      })
    );
    
    res.json({ icons: results });
  } catch (error) {
    console.error('图片处理错误详情:', error);
    res.status(500).json({ 
      error: '图片处理失败',
      details: error.message,
      stack: process.env.NODE_ENV === 'development' ? error.stack : undefined
    });
  }
});

app.listen(3000, () => {
  console.log('Server running on port 3000');
}); 
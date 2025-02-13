import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [react()],
    build: {
        rollupOptions: {
            input: ['src/main/tsx/main.tsx'],
            output: {
                entryFileNames: '[name].js',
                dir: 'src/main/resources/static/javascript',
            },
        },
        sourcemap: true,
        emptyOutDir: true,
    },
})

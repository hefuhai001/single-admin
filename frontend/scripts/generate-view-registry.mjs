/**
 * 生成视图组件注册表：用一次 fast-glob 扫描替代运行时 import.meta.glob 全量扫描。
 * 产物：src/router/modules/viewRegistry.generated.js（请勿手动修改，构建前会自动重新生成）。
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import fg from 'fast-glob'

const projectRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const viewsDir = path.join(projectRoot, 'src', 'views')
const outputFile = path.join(projectRoot, 'src', 'router', 'modules', 'viewRegistry.generated.js')

const files = await fg('**/*.vue', { cwd: viewsDir, onlyFiles: true })
files.sort()

const entries = files.map((file) => {
    const relative = file.split(path.sep).join('/')
    const key = `/src/views/${relative}`
    const importPath = `@/views/${relative}`
    return `    ${JSON.stringify(key)}: () => import(${JSON.stringify(importPath)}),`
})

const content = `// 此文件由 scripts/generate-view-registry.mjs 自动生成，请勿手动修改。
// 视图组件显式注册表：避免 import.meta.glob 在每次启动/构建时全量扫描 src/views。
export const viewModules = {
${entries.join('\n')}
}
`

fs.writeFileSync(outputFile, content, 'utf8')
console.log(`[view-registry] ${files.length} view modules -> ${path.relative(projectRoot, outputFile)}`)

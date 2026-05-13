import { createI18n } from 'vue-i18n'
import zh from './lang/zh'
import en from './lang/en'

export const i18n = createI18n({
  locale: 'zh',
  fallbackLocale: 'en',
  legacy: false,
  messages: {
    zh,
    en,
  },
})

export function $t(args: string): string {
  return i18n.global.t(args)
}

export function setLocale(locale: string): void {
  ;(i18n.global.locale as any).value = locale
}

export default i18n

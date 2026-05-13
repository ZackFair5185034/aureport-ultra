import { createApp, reactive, type App } from 'vue'
import LoadingComponent from './index.vue'

const LoadingDirective = {
  mounted(el: HTMLElement, binding: any) {
    const props = reactive({
      text: el.getAttribute('os-loading-text') || '',
      bgColor: el.getAttribute('os-loading-background') || '',
      fullscreen: binding.modifiers.fullscreen || false,
    })

    const div = document.createElement('div')
    const app = createApp(LoadingComponent, props)
    const vm = app.mount(div)

    ;(el as any)._loadingProps = props
    ;(el as any)._loadingApp = app
    ;(el as any)._loadingVm = vm

    const position = getComputedStyle(el, null).getPropertyValue('position')
    if (!position || position === 'static') {
      el.style.position = 'relative'
    }

    ;(el as any)._loadingParentDom = binding.modifiers.fullscreen ? document.body : el

    if (binding.value) {
      ;(el as any)._loadingParentDom.classList.add('u-loading-parent')
      ;(el as any)._loadingParentDom.appendChild(vm.$el as HTMLElement)
    }
  },

  updated(el: HTMLElement, binding: any) {
    const props = (el as any)._loadingProps
    if (!props) return

    props.text = el.getAttribute('os-loading-text') || ''
    props.bgColor = el.getAttribute('os-loading-background') || ''

    const parentDom = (el as any)._loadingParentDom
    const vm = (el as any)._loadingVm

    if (binding.value) {
      parentDom.classList.add('u-loading-parent')
      parentDom.appendChild(vm.$el)
    } else {
      parentDom.classList.remove('u-loading-parent')
      vm.$el.remove()
    }
  },

  unmounted(el: HTMLElement) {
    const vm = (el as any)._loadingVm
    const app: App | undefined = (el as any)._loadingApp
    if (vm && vm.$el) {
      ;(vm.$el as HTMLElement).remove()
    }
    if (app) {
      app.unmount()
    }
  },
}

const showLoading = (options?: Record<string, any>) => {
  const div = document.createElement('div')
  const app = createApp(LoadingComponent, {
    text: options?.text || '',
    bgColor: options?.bgColor || '',
    fullscreen: true,
  })
  const vm = app.mount(div)

  document.body.classList.add('u-loading-parent')
  document.body.appendChild(vm.$el as HTMLElement)

  return {
    close: () => {
      document.body.classList.remove('u-loading-parent')
      ;(vm.$el as HTMLElement).remove()
      app.unmount()
    },
  }
}

export { LoadingDirective }
export default showLoading

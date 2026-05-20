declare module 'aureport-ultra-ui' {
  export interface AureportUltra {
    install: () => void
    AureportDesignerElement: CustomElementConstructor
    AureportPreviewElement: CustomElementConstructor
    requestAdapter: any
    navigationAdapter: any
    setLibMode: (mode: boolean) => void
    getLibMode: () => boolean
    default: {
      install: () => void
      AureportDesignerElement: CustomElementConstructor
      AureportPreviewElement: CustomElementConstructor
      requestAdapter: any
      navigationAdapter: any
      setLibMode: (mode: boolean) => void
      getLibMode: () => boolean
    }
  }

  const AureportUltra: AureportUltra
  export default AureportUltra
}

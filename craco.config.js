const CracoLessPlugin = require('craco-less');

module.exports = {
  plugins: [
    {
      plugin: CracoLessPlugin,
      options: {
        lessLoaderOptions: {
          lessOptions: {
            modifyVars: {
              '@primary-color': '#667eea',
              '@link-color': '#667eea',
              '@success-color': '#52c41a',
              '@warning-color': '#faad14',
              '@error-color': '#f5222d',
              '@font-size-base': '14px',
              '@border-radius-base': '4px',
              '@btn-primary-bg': '#667eea',
              '@btn-primary-color': '#fff',
              '@input-border-color': '#d9d9d9',
              '@card-shadow': '0 2px 8px rgba(0, 0, 0, 0.09)',
              '@box-shadow-base': '0 2px 8px rgba(0, 0, 0, 0.15)'
            },
            javascriptEnabled: true
          }
        }
      }
    }
  ]
};
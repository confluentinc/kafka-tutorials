from setuptools import setup

setup(
    name='harness-runner',
    version='0.0.1',
    py_modules=['util', 'harness_runner', 'ksql'],
    scripts=['harness-runner'],
    install_requires=[
        'pyyaml==6.0',
        'pexpect==4.8.0'
    ]
)
